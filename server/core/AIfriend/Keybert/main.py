import numpy as np
import itertools

from konlpy.tag import Okt
from sklearn.feature_extraction.text import CountVectorizer
from sklearn.metrics.pairwise import cosine_similarity
from sentence_transformers import SentenceTransformer

from soynlp.tokenizer import RegexTokenizer
import pandas as pd
import warnings
from gensim.models import word2vec
import re

warnings.filterwarnings(action='ignore')

def mmr(doc_embedding, candidate_embeddings, words, top_n, diversity):
    # List of similarities between the document and each keyword
    word_doc_similarity = cosine_similarity(candidate_embeddings, doc_embedding)

    # Similarity between keywords
    word_similarity = cosine_similarity(candidate_embeddings)

    # Extract the index of keywords with the highest similarity to the document.
    # If document two had the highest similarity
    # keywords_idx = [2]
    keywords_idx = [np.argmax(word_doc_similarity)]

    # Indexes of documents other than those of keywords with the highest similarity
    # If document two had the highest similarity
    # ==> candidates_idx = [0, 1, 3, 4, 5, 6, 7, 8, 9, 10 ... omitted ...]
    candidates_idx = [i for i in range(len(words)) if i != keywords_idx[0]]

    # The best keyword has already been extracted, so repeat the following as many times as top_n-1.
    # ex) If top_n = 5, the loop below is repeated 4 times.
    for _ in range(top_n - 1):
        candidate_similarities = word_doc_similarity[candidates_idx, :]
        target_similarities = np.max(word_similarity[candidates_idx][:, keywords_idx], axis=1)

        # Calculate MMR
        mmr = (1-diversity) * candidate_similarities - diversity * target_similarities.reshape(-1, 1)
        mmr_idx = candidates_idx[np.argmax(mmr)]

        # Update keywords & candidates
        keywords_idx.append(mmr_idx)
        candidates_idx.remove(mmr_idx)

    return [words[idx] for idx in keywords_idx]


def KeyBert(chat, category_input, model_ST_input, model_W2V_input):
    doc = chat
    category = category_input
    model_ST = model_ST_input
    model_W2V = model_W2V_input
    # category = ['옷', '음악', '게임', '강아지']

    ##keybert
    ## Chatting came in
    # doc = "doc doc doc"

    # Morphological Analysis (1)
    okt = Okt()
    # Morphological Analysis (2)
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
    # Keyword embeddings
    candidate_embeddings = model_ST.encode(candidates)

    ### Extract Top 5
    # 5 settings
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

    #print("Split apply", keyword_mmr)

    for word in keyword_mmr:
        if word not in model_W2V.wv.key_to_index:
            keyword_mmr.remove(word)

    #print("Check if it is in the model", keyword_mmr)

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