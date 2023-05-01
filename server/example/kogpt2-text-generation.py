import os
import numpy as np
import torch
from model.kogpt2 import DialogKoGPT2
from transformers import PreTrainedTokenizerFast

#os.environ["CUDA_VISIBLE_DEVICES"] = "1"

root_path = '..'
checkpoint_path =f"{root_path}/checkpoint"
save_ckpt_path = f"{checkpoint_path}/kogpt2-Aihub-auto-regressive4_52959.pth"

ctx = "cuda" if torch.cuda.is_available() else "cpu"
device = torch.device(ctx)

# 저장한 Checkpoint 불러오기
checkpoint = torch.load(save_ckpt_path, map_location=device)

model = DialogKoGPT2()
model.load_state_dict(checkpoint['model_state_dict'])

model.eval()

tokenizer = PreTrainedTokenizerFast.from_pretrained("skt/kogpt2-base-v2", bos_token='<s>', eos_token='</s>', unk_token='<unk>',pad_token='<pad>', mask_token='<mask>')

count = 0
output_size = 200 # 출력하고자 하는 토큰 갯수

while 1:
  sent = input('Question: ')  # '요즘 기분이 우울한 느낌이에요'

  print(sent)
  tokenized_indexs = tokenizer.encode(sent)

  input_ids = torch.tensor([tokenizer.bos_token_id,]  + tokenized_indexs +[tokenizer.eos_token_id]).unsqueeze(0)

  sample_output = model.generate(input_ids=input_ids)


  print("Answer: " + tokenizer.decode(sample_output[0].tolist()[len(tokenized_indexs)+1:],skip_special_tokens=True))
  print(100 * '-')