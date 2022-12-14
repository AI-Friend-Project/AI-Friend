#server.py
import socket
from _thread import *

import firebase_admin
from firebase_admin import credentials
from firebase_admin import firestore

from functions import key_bert, KoGPT

import os
import numpy as np
import torch
os.environ["CUDA_VISIBLE_DEVICES"] = "1"

from konlpy.tag import Okt
from sklearn.feature_extraction.text import CountVectorizer
from sklearn.metrics.pairwise import cosine_similarity
from soynlp.tokenizer import RegexTokenizer
import pandas as pd
import warnings
from gensim.models import word2vec
import re
from sentence_transformers import SentenceTransformer
from pyfcm import FCMNotification

from model.kogpt2 import DialogKoGPT2
from transformers import PreTrainedTokenizerFast

# API 키는 비공개로 따로 저장
APIKEY = #
# 파이어베이스 콘솔에서 얻어 온 서버 키를 넣어 줌
push_service = FCMNotification(APIKEY)

#Firebase database 인증 및 앱 초기화
cred = credentials.Certificate('#')
firebase_admin.initialize_app(cred,{
    'projectId' : '#'
})

db = firestore.client()

#model loading
root_path = '../..'
checkpoint_path =f"{root_path}/checkpoint"
save_ckpt_path = f"{checkpoint_path}/kogpt2-wellnesee-auto-regressive4_52959.pth"

ctx = "cuda" if torch.cuda.is_available() else "cpu"
device = torch.device(ctx)

# 저장한 Checkpoint 불러오기
checkpoint = torch.load(save_ckpt_path, map_location=device)

model = DialogKoGPT2()
model.load_state_dict(checkpoint['model_state_dict'])

model.eval()
tokenizer = PreTrainedTokenizerFast.from_pretrained("skt/kogpt2-base-v2", bos_token='<s>', eos_token='</s>', unk_token='<unk>',pad_token='<pad>', mask_token='<mask>')

#Keybert loading
model_ST = SentenceTransformer('sentence-transformers/xlm-r-100langs-bert-base-nli-stsb-mean-tokens')
model_W2V = word2vec.Word2Vec.load('./Keybert/model_W2V')
category = ['여행', '음악', '게임', '동물', '옷', '음식', '운동', '독서', '요리']


#유저 소켓 정보 리스트
user_sockets = []

def getipaddrs(hostname):# Just to show IP , just to test it   
    result = socket.getaddrinfo(hostname, None, 0, socket.SOCK_STREAM)  
    return [x[4][0] for x in result]

#쓰레드 생성
def threaded(client_socket, addr):
    print('>> Connected by : ' + addr[0] + ':' + str(addr[1]))

    while True:
        try:
            data = client_socket.recv(2048)
            if not data:
                print('>> Disconnected by : ' + addr[0] + ':' + str(addr[1]))
                break
            print('>> Received from : ' + addr[0] + ':' + str(addr[1]))
            try:
                print('>> ' + data.decode('utf-8'))
            except:
                if client_socket in user_sockets:
                    user_sockets.remove(client_socket)
                    print("Current user : ", len(user_sockets))
                break

            original_data = data.decode('utf-8')

            # 여기 밑으로 유저 함수 추가
            try:
                if original_data[:6] == 'AIchat':
                    uid = original_data[6:]
                    KoGPT(uid, db, model, tokenizer, push_service, model_ST, model_W2V, category)
                else:
                    print('unexpected access')
                    if client_socket in user_sockets:
                        user_sockets.remove(client_socket)
                        print("Current user : ", len(user_sockets))
                    break
            except:
                if client_socket in user_sockets:
                    user_sockets.remove(client_socket)
                    print("Current user : ", len(user_sockets))
                break
        except ConnectionResetError as e:
            print('>> Disconnected by : ' + addr[0] + ':' + str(addr[1]))
            break
    if client_socket in user_sockets:
        user_sockets.remove(client_socket)
        print("Current user : ", len(user_sockets))

#파이썬 서버 소켓 구축
host = '#' # Empty represents local host
hostname = socket.gethostname()
hostip = getipaddrs(hostname)  
print('host ip', hostip)# Should be displayed as: 127.0.1.1  
port = '#' # Arbitrary non-privileged port

server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
server_socket.bind((host, port))
server_socket.listen()

try:
    while True:
        print('>> 새로운 연결 대기중')
        client_socket, addr = server_socket.accept()  
        user_sockets.append(client_socket)
        print("Current user : ", len(user_sockets))
        start_new_thread(threaded, (client_socket, addr))
        
        
except Exception as e:
    print('error : ',e)
    
finally:
    server_socket.close()
