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
    # 문서와 각 키워드들 간의 유사도가 적혀있는 리스트
    word_doc_similarity = cosine_similarity(candidate_embeddings, doc_embedding)

    # 각 키워드들 간의 유사도
    word_similarity = cosine_similarity(candidate_embeddings)

    # 문서와 가장 높은 유사도를 가진 키워드의 인덱스를 추출.
    # 만약, 2번 문서가 가장 유사도가 높았다면
    # keywords_idx = [2]
    keywords_idx = [np.argmax(word_doc_similarity)]

    # 가장 높은 유사도를 가진 키워드의 인덱스를 제외한 문서의 인덱스들
    # 만약, 2번 문서가 가장 유사도가 높았다면
    # ==> candidates_idx = [0, 1, 3, 4, 5, 6, 7, 8, 9, 10 ... 중략 ...]
    candidates_idx = [i for i in range(len(words)) if i != keywords_idx[0]]

    # 최고의 키워드는 이미 추출했으므로 top_n-1번만큼 아래를 반복.
    # ex) top_n = 5라면, 아래의 loop는 4번 반복됨.
    for _ in range(top_n - 1):
        candidate_similarities = word_doc_similarity[candidates_idx, :]
        target_similarities = np.max(word_similarity[candidates_idx][:, keywords_idx], axis=1)

        # MMR을 계산
        mmr = (1-diversity) * candidate_similarities - diversity * target_similarities.reshape(-1, 1)
        mmr_idx = candidates_idx[np.argmax(mmr)]

        # keywords & candidates를 업데이트
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
    ## 채팅이 들어옴
    # doc = "강아지(puppy, 프랑스어: Chiot)는 개의 새끼를 일컫는다.[1] 강아지는 성체로 발달하는 과정에 있으므로 자라면서 털색이나 체형 등이 달라질 수 있으며[2], 정서적인 변화를 겪기도 한다.[3] 강아지 알레르기가 있으면 강아지를 피하는 것이 좋다. 명칭 한국어 ‘강아지’는 ‘개’에 어린 짐승을 뜻하는‘아지'가 붙은 말이다. 경상북도 사투리로는 강생이라고도 한다. 각 언어마다 강아지에 대한 별도의 명칭이 있는데, 영어로는 강아지를 ‘Puppy’(퍼피)라 부르며, 일본어로는 ‘小犬(코이누)’라 하며, 중국어로는 '小狗(샤오거우)' 혹은 幼犬(유추안)이라 한다. 발달 태어난 강아지는 생후 10일까지를 출생견으로 본다. 강아지는 견종에 따라 체중이 매우 다양하며 태어난 지 14일 무렵 눈을 뜨고 소리에 반응하며 걷기 시작한다.[4] 몸떨기 반사는 그보다 1주일 정도 빠르다. 젖 빨기는 첫 2주간은 2시간마다 이루어지며 8일 만에 몸무게가 약 두 배로 늘어나는 등 빠르게 성장한다. 이 기간엔 따로 마련한 출산장에서 강아지의 관리를 전적으로 어미개에게 맡기는 것이 좋다.[5] 강아지가 자신의 몸을 부들부들 떠는 몸떨기 반사는 체온을 유지하기 위한 자연스러운 반응이다. 강아지는 생후 3주에서 3개월에 걸쳐 한 배에서 나온 강아지들과 어미, 사람들과 사회적 관계를 맺는 이행기를 거친다. 이 시기 강아지들에겐 나중에 어미나 같이 태어난 강아지와 떨어져 홀로 살아갈 앞날을 위해 다른 동물들과 마주치는 훈련등 사회화가 필요하다. 이행기의 강아지는 호기심이 왕성하여 바닥에 떨어진 것은 전기 코드나 작은 물건을 가리지 않고 물어뜯기 때문에 안전에 주의하여야 한다.[6] 생후 20일 - 30일까지 젖을 먹으며 그 이후엔 어미의 먹이에 관심을 갖는다.[7] 생후 3주부터 이유식을 준비하여 준다. 이유식은 습기가 많은 물렁물렁한 음식으로 시작하며 시리얼, 아기밀, 다진 고기, 스크램블 에그 등을 혼합하여 고단백 고탄수화물 음식을 준다.[6] 일반적으로 생후6주~8주의 기간에 예방접종을 실시하고있다. 이러한 1차접종후 1~2회의 추가접종이 이루어지고 있다. 다만 광견병예방접종은 생후3개월이후부터 기초접종을 실시하고 있다.[8] 사회화 다른 강아지와 함께 노는 강아지의 사회화 강아지의 서열 개는 매우 강한 사회성을 보이는 동물이며 강아지는 어미나 자신들끼리 노는 데 대부분의 시간을 보낸다. 사람과의 사회적 관계를 맺는 것은 8주에서 12주 무렵부터 이루어지며 점차 사람과 상호 반응하는 사회적 기술을 익히게 된다. 이 시기에 사람이나 성체 개로부터 위협을 겪으면 정서적으로 크게 위축될 수 있다.[12][13] 여러 강아지들은 주인에 대한 충성심이 매우 강하다. 강아지는 주인 또는 우두머리로부터 통제받고 명령받는 것을 배워야 한다. 정확한 규칙과 규율에 따라 기르지 못한 개는 스트레스를 받으며 자신이 집단이나 가족의 리더가 되려고 하는 경향을 보인다.[14] 일반적으로 강아지 역시 다른 동물들처럼 경계심과 공격성의 방어적 행동과 스트레스, 순응, 장난(또는 놀이,play)등의 주요한 사회적 행동 양상을 발달과 성장 단계에서 보여준다. 특히 순응과 장난은 사회화의 주요 메커니즘이며 스트레스는 방어적 행동과 사회화의 과정에서 주의 깊은 배려가 필요하다. 덜 방어적이고 보다 더 순응적인 행동이 사회화의 주요한 과정일 수 있지만 스트레스가 경계심과 공격성을 강화하도록 하는 것을 완화 또는 우회하도록 하는 사람과 강아지와의 정서적 교감은 이러한 맥락에서 다른 요인보다 우선적으로 다루어지도록 고려해야 할 필요성이 있다. 예방 접종 강아지는 어미의 젖을 떼면 모체로부터 주어지던 면역 기능이 중단되어 병에 걸리기 쉽게 된다. 애완견의 주요 사망 시기는 생후 2개월 미만의 강아지 때가 아니면 10살 이상의 고령견이 대부분이다. 전염병의 방지를 위해 모견부터 철저히 예방 접종을 하고 강아지도 3차에 걸쳐 예방 접종을 하는 것이 좋다.[15] 예방 접종은 보통 5종 또는 7종의 질병에 대한 종합 예방 접종을 실시하며 동물 병원에서 항체 검사를 통해 부족한 백신을 추가로 접종하는 것이 좋다.[16]접종이 모두 끝나면 산책도 할 수 있으며, 다른 강아지와의 접촉도 가능하다. 1차 예방 접종은 생후 6-8주 사이에 이루어지며 이후 2 - 3 주 간격을 두고 추가적인 접종을 한다. 접종 백신으로는 일반적으로 DHPPL(Canine Distemper(개 홍역), Hepatitis(전염성 간염), Parainfluenza(파라인플루엔자), Parvo Virus Infection(파르보 장염), Leptospira(렙토스피라)혼합주사)의 5종 종합 백신이 실시되고 있으며 코로나 장염 백신(생후 6-8주) 및 광견병 백신(생후 3개월 이상)이 주령에 맞게 실시되고 있다. 또한 켄넬코프 백신, 개 인플루엔자 등이 권장될 수 있다.[17][18]"

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

    #print("분할 적용", keyword_mmr)

    for word in keyword_mmr:
        if word not in model_W2V.wv.key_to_index:
            keyword_mmr.remove(word)

    #print("모델에 있는지 확인", keyword_mmr)

    max_score = 0.8
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