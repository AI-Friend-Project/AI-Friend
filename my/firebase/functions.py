import firebase_admin
from firebase_admin import credentials
from firebase_admin import firestore
import torch
import time
import os
import sys
import warnings
from Keybert.main import KeyBert
from konlpy.tag import Okt
from Keybert.main import mmr
from sklearn.metrics.pairwise import cosine_similarity
from sklearn.feature_extraction.text import CountVectorizer
import random

warnings.filterwarnings(action='ignore')

def getToken(uid, db):
    # user 컬렉션 찾기
    # db 는 aifriend 프로젝트 db
    doc_ref = db.collection(u'user')
    # user 컬렉션에서 uid로 유저 찾기
    users = doc_ref.where('uid', u'==', uid).stream()
    # 해당 유저 정보 가져오기

    user_data = []
    for doc in users:
        user_data.append(doc.to_dict())

    # 해당 유저의 토큰 가져오기
    token = user_data[0]['token']

    return token

def sendMessage(title, body, token, push_service):
    # 메시지 (data 타입)
    data_message = {
        "title": title,
        "body": body
    }
    # 토큰값을 이용해 유저에게 푸시알림을 전송함
    result = push_service.single_device_data_message(registration_id=token, data_message=data_message)
    # 전송 결과 출력
    print(result)

def KoGPT(user_id, database, model, tokenizer, push_service, model_ST, model_W2V, category, user_base_delay = 5):
    uid = user_id
    db = database
    model = model
    tokenizer = tokenizer
    user_base_delay = user_base_delay

    # Delay method
    randint = random.randint(1,10)
    if randint <= 6:
        user_base_delay += random.randint(5,15)
    elif (randint > 6) & (randint <=9) :
        user_base_delay += random.randint(25,55)
    elif randint > 9:
        user_base_delay += random.randint(115,175)

    #user_base_delay = 1
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

    print(user_chat_list[0])

    tokenized_indexs = tokenizer.encode(user_chat_list[0])
    input_ids = torch.tensor([tokenizer.bos_token_id, ] + tokenized_indexs + [tokenizer.eos_token_id]).unsqueeze(0)
    # set top_k to 50
    sample_output = model.generate(input_ids=input_ids)

    answer = tokenizer.decode(sample_output[0].tolist()[len(tokenized_indexs) + 1:], skip_special_tokens=True)

    print(answer)

    answer = answer.replace(' 키키', '')
    answer = answer.replace('키키 ', '')
    answer = answer.replace('키키', '')

    AIchat_ref.add({'message': answer, 'time': firestore.SERVER_TIMESTAMP, 'uid': 'AIfriend'})

    # AI 채팅 파이어스토어에 등록할 때 넣기
    sendMessage("AI", answer, getToken(uid, db), push_service)
    db.collection(u'AIChat').document(document_name).update({'check': [0, 1]})

    # Keybert
    try:
        keybert_check = db.collection(u'AIChat').document(document_name).get().to_dict()['userLeng']
    except:
        db.collection(u'AIChat').document(document_name).update({'userLeng': 0})
        keybert_check = -1

    if keybert_check != -1:
        keybert_check = keybert_check % 10
    if keybert_check == 0:
        bert_keyword = key_bert(uid, db, model_ST, model_W2V, category)
        if bert_keyword in category:
            email = db.collection(u'user').where('uid', '==', uid).get()[0].id
            check = db.collection("fav").document(bert_keyword).get().to_dict()['users']
            if email in check:
                print('already in fav ' + bert_keyword)
            else:
                db.collection("fav").document(bert_keyword).update({"users": firestore.ArrayUnion([email])})
                AIchat_ref.add({'message': bert_keyword + '에 관심있구나! 내가 비슷한 취향을 가진 친구들을 소개시켜줄게! 내 관심사 탭에 가볼래?', 'time': firestore.SERVER_TIMESTAMP, 'uid': 'AIfriend'})
            keybert_check = 0
        else:
            print('keyword is not matched : ', bert_keyword)

    if keybert_check == -1:
        keybert_check += 1
    db.collection(u'AIChat').document(document_name).update({'lastChat': answer, 'check':[0,1], 'userLeng':keybert_check+1})


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
    print('doc : ' + doc)

    # 형태소 분석 (1)
    okt = Okt()
    # 형태소 분석 (2)
    tokenized_doc = okt.pos(doc)
    # 명사만 추출
    tokenized_nouns = ' '.join([word[0] for word in tokenized_doc if word[1] == 'Noun'])

    # print('품사 태깅 10개만 출력 :',tokenized_doc[:10])
    # print('명사 추출 :',tokenized_nouns)

    n_gram_range = (2, 3)

    count = CountVectorizer(ngram_range=n_gram_range).fit([tokenized_nouns])
    candidates = count.get_feature_names()

    # print('trigram 개수 :',len(candidates))
    # print('trigram 다섯개만 출력 :',candidates[:5])

    # model_ST = SentenceTransformer('sentence-transformers/xlm-r-100langs-bert-base-nli-stsb-mean-tokens')
    # 문서 임베딩
    doc_embedding = model_ST.encode([doc])
    # 키워드 임베딩
    candidate_embeddings = model_ST.encode(candidates)

    ### 상위 5개 추출
    # 5개 설정
    top_n = 5
    # 코사인 유사도 계산 (문서, 키워드)
    distances = cosine_similarity(doc_embedding, candidate_embeddings)
    keywords = [candidates[index] for index in distances.argsort()[0][-top_n:]]
    print(keywords)

    """# **Maximal Marginal Relevance**"""

    ## 실제로 돌아갈 함수!!
    keyword_mmr = mmr(doc_embedding, candidate_embeddings, candidates, top_n=5, diversity=0.7)

    # model_W2V_name = 'model_W2V'
    # model_W2V = word2vec.Word2Vec.load(model_W2V_name)

    for kw in keyword_mmr:
        keyword_mmr.remove(kw)
        tmp = kw.split()
        keyword_mmr = keyword_mmr + tmp

    # print("분할 적용", keyword_mmr)
    keyword_mmr = list(set(keyword_mmr))
    random.shuffle(keyword_mmr)

    for word in keyword_mmr[:]:
        if word not in model_W2V.wv.key_to_index.keys():
            keyword_mmr.remove(word)

    # print("모델에 있는지 확인", keyword_mmr)

    max_score = 0.79999
    max_ctg = ""
    max_kw = ""

    for ctg in category:
        for word in keyword_mmr:
            score = model_W2V.wv.similarity(ctg, word)
            if score > 0.8 and max_score < score:
                max_score = score
                max_ctg = ctg
                max_kw = word

    print(max_ctg, max_kw, max_score)
    return max_ctg