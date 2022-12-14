import os

root_path = "../data"
file_path = root_path + "/kakao"
o_path = root_path + "/kakao_data_test_cnt.txt"

o_file = open(o_path, 'w')

txt_files = []
for (root, directories, files) in os.walk(file_path):
    for file in files:
        if '.txt' in file:
            txt_file_path = os.path.join(root, file)
            txt_files.append(txt_file_path)

whole_sentence_len = 0
cnt = 0
doc_cnt = 0
for file_name in txt_files:
    # if cnt > 1000:
    #     exit()
    with open(file_name,"r") as i_file:
        doc_whole = i_file.read()

        if '3' in doc_whole:
            continue
        if '여성 인권' in doc_whole:
            continue
        if '미투' in doc_whole:
            continue
        if '정치' in doc_whole:
            continue
        if '대통령' in doc_whole:
            continue
        if '연대' in doc_whole:
            continue
        if '탄압' in doc_whole:
            continue
        if '언론' in doc_whole:
            continue
        if '일베' in doc_whole:
            continue
        if '재인' in doc_whole:
            continue
        if '근혜' in doc_whole:
            continue
        if '명박' in doc_whole:
            continue

        doc_whole = doc_whole.split('\n')
        while True:
            try:
                doc_whole.remove('')
            except:
                break

        for i in doc_whole:
            if type(i[0]) != int:
                continue
        doc_cnt += 1
        doc_len = len(doc_whole)
        for i in range(doc_len):
            #1,1,2 / 2,2,1 sentence
            if doc_len-1 > i+1:
                if (doc_whole[i][0] == doc_whole[i+1][0]) and (doc_whole[i+1][0] != doc_whole[i+2][0]):
                    temp_1 = doc_whole[i][4:] + ' ' + doc_whole[i+1][4:]
                    temp_2 = doc_whole[i+2][4:]
                    sentence_combine = temp_1 + "\t" + temp_2 + "\n"
                    if '*' in sentence_combine:
                        continue
                    if temp_1 == '' or temp_2 == '':
                        continue
                    o_file.write(sentence_combine)
                    cnt += 1
                    continue

            # 1,2 / 2,1 sentence
            if doc_len-1 > i:
                if (doc_whole[i][0] != doc_whole[i+1][0]):
                    if len(doc_whole[i][4:]) < 4:
                        continue

                    temp_1 = doc_whole[i][4:]
                    temp_2 = doc_whole[i+1][4:]
                    sentence_combine = temp_1 + "\t" + temp_2 + "\n"
                    if '*' in sentence_combine:
                        continue
                    if temp_1 == '' or temp_2 == '':
                        continue
                    o_file.write(sentence_combine)
                    cnt += 1
                    continue

            else:
                continue
o_file.close()
print(cnt)
print(doc_cnt)