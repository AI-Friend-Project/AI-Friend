# AI Friend 

### What is AI Friend?
An application that identifies users' interests while talking to chatbots and matches people with similar interests.

> AI-Friend can
>> be a good friend for people who lonely and have difficulty in relationships.</br>
>> help users easily make new relationships with analysis and expansion.


# System Architecture
<!-- <img width="655" alt="image" src="https://user-images.githubusercontent.com/65584699/207514631-0d3b7497-d245-4488-9daf-0ba4ac0ce884.png"> -->
<img width="700" alt="image" src="https://user-images.githubusercontent.com/65584699/207621532-65b62380-c6f1-4331-ac4e-b97095ea3bdb.png">

# App Flow
<!-- ![app Flow](https://user-images.githubusercontent.com/65584699/207540029-72d17922-d7aa-4056-acc1-0ebe41dfb9ea.png)
 -->
<img width="700" alt="image" src="https://user-images.githubusercontent.com/65584699/207602835-d74f978b-9ee3-4484-9960-96c44b522a23.png">

# Key Features
Detailed features and screens are described later.
- Chat with AI
<img width="400" alt="image" src="https://user-images.githubusercontent.com/65584699/207515482-cd0a6a60-05bf-4111-b4a9-406f2c3b5626.png">

- Identify and recommend interests
<img width="400" alt="image" src="https://user-images.githubusercontent.com/65584699/207515865-80195b02-4503-4101-abd7-556f33781532.png">


# Used AI Model
We used **AIHub**'s topic-specific text-daily conversation data set.
- link : <https://aihub.or.kr/aihubdata/data/view.do?currMenu=115&topMenu=100&aihubDataSe=realm&dataSetSn=543/>
## GPT-2
![ezgif com-gif-maker](https://user-images.githubusercontent.com/65584699/207517716-49d0bfd7-9422-46c7-afff-246298a9cb5a.gif)


## KeyBERT
Extract keywords from users' chat content
## Word2Vec
Calculate word similarity between keywords and categories, Connect one category with the highest similarity to the user
</br>

---

## Tech Stack
| Field | |
|----------|:-------------:|
|__Client__| <img src="https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=Kotlin&logoColor=white"> <img src="https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=Android&logoColor=white"> <img src="https://img.shields.io/badge/Android Studio-3DDC84?style=for-the-badge&logo=Android Studio&logoColor=white">|
|__Backend & Server__| <img src="https://img.shields.io/badge/Python-3776AB?style=for-the-badge&logo=Python&logoColor=white"> |
|__DB__| <img src="https://img.shields.io/badge/Firebase-FFCA28?style=for-the-badge&logo=Firebase&logoColor=black"> |
|__AI__| GPT-2, KeyBERT, Word2Vec|
|__Others__| <img src="https://img.shields.io/badge/Git-F05032?style=for-the-badge&logo=Git&logoColor=black"> |


### Members 
| Name | Field |
| ------ | ------ |
| 곽민선 | Data Search |
| 이가현 | Client, Word2Vec | 
| 이예준 | Backend & Server, GPT-2|
| 이지윤 | Client, KeyBERT |





 

