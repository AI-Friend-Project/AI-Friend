import torch
import torch.nn as nn
from torch.utils.data import Dataset

from transformers import PreTrainedTokenizerFast

class AIhubAutoRegressiveDataset(Dataset):
  """AIhub Auto Regressive Dataset"""

  def __init__(self, file_path = "../data/chatbot_wellness_dialog_for_autoregressive.txt", n_ctx = 128+64):
    self.file_path = file_path
    self.data =[]
    self.tokenizer = PreTrainedTokenizerFast.from_pretrained("skt/kogpt2-base-v2", bos_token='<s>', eos_token='</s>', unk_token='<unk>',pad_token='<pad>', mask_token='<mask>')

    bos_token_id = [self.tokenizer.bos_token_id]
    eos_token_id = [self.tokenizer.eos_token_id]
    pad_token_id = [self.tokenizer.pad_token_id]

    file = open(self.file_path, 'r', encoding='utf-8')

    while True:
      line = file.readline()
      if not line:
        break
      datas = line.split("\t")
      index_of_words = bos_token_id +self.tokenizer.encode(datas[0]) + eos_token_id + bos_token_id + self.tokenizer.encode(datas[1][:-1])+ eos_token_id
      pad_token_len = n_ctx - len(index_of_words)

      index_of_words += pad_token_id * pad_token_len

      if len(index_of_words) != n_ctx:
        continue

      self.data.append(index_of_words)

    file.close()

  def __len__(self):
    return len(self.data)

  def __getitem__(self,index):
    item = self.data[index]
    return item