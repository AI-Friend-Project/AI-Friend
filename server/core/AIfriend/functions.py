import firebase_admin
from firebase_admin import credentials
from firebase_admin import firestore
import torch
import time
import os
import sys
import warnings
from konlpy.tag import Okt
from Keybert.main import mmr
from sklearn.metrics.pairwise import cosine_similarity
from sklearn.feature_extraction.text import CountVectorizer
import random

warnings.filterwarnings(action='ignore')

def getToken(uid, db):
    # Find 'user' Collection
    # ref to aifrend project db
    doc_ref = db.collection(u'user')
    # Find users with 'uid' in 'user collection'
    users = doc_ref.where('uid', u'==', uid).stream()

    user_data=[]
    # Get the user information
    for doc in users:
        user_data.append(doc.to_dict())

    # Get tokens for that user
    token = user_data[0]['token']

    return token

def sendMessage(title, body, token, push_service):
    # Message (data type)
    data_message = {
        "title": title,
        "body": body
    }

    # Send push notification to user using token value
    result = push_service.single_device_data_message(registration_id=token, data_message=data_message)
    # Print Result Output
    # print(result)

def KoGPT(user_id, database, model, tokenizer, push_service, model_ST, model_W2V, category, user_base_delay = 5, fav_max_count = 50):
    uid = user_id
    db = database
    model = model
    tokenizer = tokenizer
    user_base_delay = user_base_delay

    # Delay method
    user_base_delay = chatting_delay(user_base_delay)

    #user_base_delay = 1 <- delay is 1 second
    time.sleep(user_base_delay)

    # Document search
    document_name = db.collection(u'AIChat').where('uid', 'array_contains', uid).get()[0].id
    AIchat_ref = db.collection(u'AIChat').document(document_name).collection('Chats')

    query = AIchat_ref.order_by(u"time", direction=firestore.Query.DESCENDING).limit(3)
    chats = list(query.get())

    user_chat_list = []
    AI_chat_list = []
    for i in chats:
        try:
            if i.to_dict()['uid'] == uid:
                user_chat_list.append(i.to_dict()['message'])
            else:
                AI_chat_list.append(i.to_dict()['message'])
        except:
            continue

    # print user chatting log
    # print(user_chat_list[0])

    tokenized_indexs = tokenizer.encode(user_chat_list[0])
    input_ids = torch.tensor([tokenizer.bos_token_id, ] + tokenized_indexs + [tokenizer.eos_token_id]).unsqueeze(0)

    sample_output = model.generate(input_ids=input_ids)

    answer = tokenizer.decode(sample_output[0].tolist()[len(tokenized_indexs) + 1:], skip_special_tokens=True)

    # AI friend answer logging
    # print(answer)

    # Model outputs have so many '키키' in sentence, and delete it.
    answer = answer.replace(' 키키', '')
    answer = answer.replace('키키 ', '')
    answer = answer.replace('키키', '')
    
    # Save the AIFriend answer to user own firestore db
    AIchat_ref.add({'message': answer, 'time': firestore.SERVER_TIMESTAMP, 'uid': 'AIfriend'})

    # Sending message 
    sendMessage("AI", answer, getToken(uid, db), push_service)
    db.collection(u'AIChat').document(document_name).update({'check': [0, 1]})

    # For using Keybert, checking user variable in firestore
    try:
        keybert_check = db.collection(u'AIChat').document(document_name).get().to_dict()['userLeng']
    except:
        db.collection(u'AIChat').document(document_name).update({'userLeng': 0})
        keybert_check = -1

    # In each 'fav_max_count', server starts extracting interests
    if keybert_check != -1:
        keybert_check = keybert_check % fav_max_count
    if keybert_check == 0:
        # Category connecting each 'fav_max_count' user chatting
        category_connect(uid, db, model_ST, model_W2V, category)

    # Change the last chatting in user own firestore db
    if keybert_check == -1:
        keybert_check += 1
    db.collection(u'AIChat').document(document_name).update({'lastChat': answer, 'check':[0,1], 'userLeng':keybert_check+1})

def chatting_delay(base_delay):
    user_base_delay = base_delay
    randint = random.randint(1,10)
    
    if randint <= 6:
        user_base_delay += random.randint(5,15)
    elif (randint > 6) & (randint <=9) :
        user_base_delay += random.randint(25,55)
    elif randint > 9:
        user_base_delay += random.randint(115,175)
    
    return user_base_delay
    
def category_connect(uid, db, model_ST, model_W2V, category):
    bert_keyword = key_bert(uid, db, model_ST, model_W2V, category)
    if bert_keyword in category:
        email = db.collection(u'user').where('uid', '==', uid).get()[0].id
        check = db.collection("fav").document(bert_keyword).get().to_dict()['users']
        if email in check:
            # server log for checking error
            # print('already in fav ' + bert_keyword)
        else:
            db.collection("fav").document(bert_keyword).update({"users": firestore.ArrayUnion([email])})
            AIchat_ref.add({'message': bert_keyword + '에 관심있구나! 내가 비슷한 취향을 가진 친구들을 소개시켜줄게! 내 관심사 탭에 가볼래?', 'time': firestore.SERVER_TIMESTAMP, 'uid': 'AIfriend'})
    else:
        # server log for checking error
        # print('keyword is not matched : ', bert_keyword)

def key_bert(user_id, database, model_ST, model_W2V, category):
    uid = user_id
    db = database

    document_name = db.collection(u'AIChat').where('uid', 'array_contains', uid).get()[0].id
    AIchat_ref = db.collection(u'AIChat').document(document_name).collection('Chats')

    query = AIchat_ref.order_by(u"time", direction=firestore.Query.DESCENDING).limit(20)
    chats = list(query.get())

    doc = ''

    for i in chats:
        if i.to_dict()['uid'] == uid:
            doc += i.to_dict()['message'] + ' '
        else:
            continue
    print(doc)

    # Morphological analysis (1)
    okt = Okt()
    # Morphological analysis (2)
    tokenized_doc = okt.pos(doc)
    # Extract nouns only
    tokenized_nouns = ' '.join([word[0] for word in tokenized_doc if word[1] == 'Noun'])

    # print('Only 10 parts tagging are output :',tokenized_doc[:10])
    # print('Noun extraction :',tokenized_nouns)

    n_gram_range = (2, 3)

    count = CountVectorizer(ngram_range=n_gram_range).fit([tokenized_nouns])
    candidates = count.get_feature_names()

    # print('trigram :',len(candidates))
    # print('print only 5 trigram :',candidates[:5])

    # model_ST = SentenceTransformer('sentence-transformers/xlm-r-100langs-bert-base-nli-stsb-mean-tokens')
    # Document embedding
    doc_embedding = model_ST.encode([doc])
    # Keyword embedding
    candidate_embeddings = model_ST.encode(candidates)

    ### Extract Top 5
    # top 5 setting
    top_n = 5
    # Cosine similarity calculation (document, keyword)
    distances = cosine_similarity(doc_embedding, candidate_embeddings)
    keywords = [candidates[index] for index in distances.argsort()[0][-top_n:]]
    print(keywords)

    """# **Maximal Marginal Relevance**"""

    ## Main Function
    keyword_mmr = mmr(doc_embedding, candidate_embeddings, candidates, top_n=5, diversity=0.7)

    # model_W2V_name = 'model_W2V'
    # model_W2V = word2vec.Word2Vec.load(model_W2V_name)

    for kw in keyword_mmr:
        keyword_mmr.remove(kw)
        tmp = kw.split()
        keyword_mmr = keyword_mmr + tmp

    # print("Split apply", keyword_mmr)
    keyword_mmr = list(set(keyword_mmr))
    random.shuffle(keyword_mmr)

    for word in keyword_mmr[:]:
        if word not in model_W2V.wv.key_to_index.keys():
            keyword_mmr.remove(word)

    # print("Check if it is in the model", keyword_mmr)

    max_score = 0.79999
    max_ctg = ""
    max_kw = ""

    for ctg in category:
        for word in keyword_mmr:
            score = model_W2V.wv.similarity(ctg, word)
            print(ctg, word, score)

            if score > 0.8 and max_score < score:
                max_score = score
                max_ctg = ctg
                max_kw = word

    print(max_ctg, max_kw, max_score)
    return max_ctg
