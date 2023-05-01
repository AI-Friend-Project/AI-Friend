import os
#os.environ['CUDA_VISIBLE_DEVICES'] = "1"
import numpy as np
from tqdm import tqdm

import torch
from torch.utils.data import dataloader
from dataloader.dataset import AIhubAutoRegressiveDataset
from model.kogpt2 import DialogKoGPT2

if __name__ == '__main__':
    data_path = "../data/kakao_data.txt"
    checkpoint_path ="../checkpoint"
    save_ckpt_path = f"{checkpoint_path}/kogpt2-Aihub-auto-regressive.pth"

    n_epoch = 5         # Num of Epoch
    batch_size = 12      # 배치 사이즈
    device = torch.device('cuda')
    save_step = 25000 # 학습 저장 주기
    learning_rate = 5e-5/3 # Learning Rate

    dataset= AIhubAutoRegressiveDataset(data_path)
    train_loader = torch.utils.data.DataLoader(dataset, batch_size=batch_size, shuffle=True)

    model = DialogKoGPT2()

    loss_fct = torch.nn.CrossEntropyLoss(ignore_index=3)
    optimizer = torch.optim.Adam(model.parameters(), lr=learning_rate)

    losses =[]
    model.to(device)
    loss_fct.to(device)

    for epoch in range(n_epoch):
        count = 0
        with tqdm(total=len(train_loader), desc=f"Train({epoch})") as pbar:
            for i, data in enumerate(train_loader):
                optimizer.zero_grad()
                data = torch.stack(data)  # list of Tensor, list -> stack.
                data = data.transpose(1, 0)

                data = data.to(device)

                outputs = model(data, labels=data)
                _, logits = outputs[:2]

                # Shift so that tokens < n predict n
                shift_logits = logits[..., :-1, :].contiguous()
                shift_labels = data[..., 1:].contiguous()

                loss = loss_fct(shift_logits.view(-1, shift_logits.size(-1)), shift_labels.view(-1))
                loss.backward()
                optimizer.step()

                losses.append(loss.item())

                if count % 10000 == 0:
                    print('epoch no.{} train no.{}  loss = {}'.format(epoch, count + 1, loss))
                if (count > 0 and count % save_step == 0) or (len(data) < batch_size):
                    torch.save({
                        'epoch': epoch,
                        'train_no': count,
                        'model_state_dict': model.state_dict(),
                        'optimizer_state_dict': optimizer.state_dict(),
                        'loss': loss
                    }, save_ckpt_path[:-4]+str(epoch)+'_'+str(count)+'.pth')
                count += 1
                pbar.update(1)
                pbar.set_postfix_str(f"Loss: {loss.item():.3f} ({np.mean(losses):.3f})")
