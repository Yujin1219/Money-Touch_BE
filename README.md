# 🌱 UMC 8th 돈터치 Backend 🌱
## 🤑 Introduce
<img width="1920" height="1080" alt="Image" src="https://github.com/user-attachments/assets/c9ee2c8b-74e9-4162-88a2-c3e651be45f8" />

**돈터치** 서비스는 혼자가 아닌 ‘함께’ 소비를 관리하는 SNS형 소비 관리 웹앱 서비스입니다.
이름의 뜻은 돈 + 터치와 dont touch 두 가지로, 돈터치가 지출 유혹을 멈출 수 있도록 터치해주겠다는 의미입니다.
SNS 형식의 소비 피드와 랭킹·배지 시스템을 통해 사용자 간 소통과 경쟁을 활성화하며, 가계부·소비 루틴·고정비 관리 등 체계적인 소비 관리 서비스를 제공합니다.

## 🔧 Tech Stack
<p>
<img src="https://img.shields.io/badge/springboot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white">
<img src="https://img.shields.io/badge/Java-007396?style=for-the-badge&logo=openjdk&logoColor=white">
<img src="https://img.shields.io/badge/JWT-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white">
<img src="https://img.shields.io/badge/OAuth2.0-3C3C3D?style=for-the-badge&logo=oauth&logoColor=white">
<img src="https://img.shields.io/badge/SendGrid-0085CA?style=for-the-badge&logo=sendgrid&logoColor=white">
<img src="https://img.shields.io/badge/Redis-DC382D?style=for-the-badge&logo=redis&logoColor=white">
</p>
<p>
  <img src="https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white">
  <img src="https://img.shields.io/badge/Nginx-009639?style=for-the-badge&logo=nginx&logoColor=white">
  <img src="https://img.shields.io/badge/AWS-232F3E?style=for-the-badge&logo=amazonaws&logoColor=white">
  <img src="https://img.shields.io/badge/github-181717?style=for-the-badge&logo=github&logoColor=white">
  <img src="https://img.shields.io/badge/GitHub_Actions-2088FF?style=for-the-badge&logo=githubactions&logoColor=white">
  <img src="https://img.shields.io/badge/Swagger-85EA2D?style=for-the-badge&logo=swagger&logoColor=white">
</p>

## ⭐ Main Feature
<img width="3840" height="2160" alt="image" src="https://github.com/user-attachments/assets/7e48191e-0856-49d5-a9d7-f57efb973f16" />
<img width="3840" height="4320" alt="image" src="https://github.com/user-attachments/assets/c3003787-6eed-4117-8adb-dcbd4700c3c5" />
<img width="3840" height="5502" alt="image" src="https://github.com/user-attachments/assets/9d2fdafc-d5b6-4964-87ff-c106bca6237d" />
<img width="3840" height="8040" alt="image" src="https://github.com/user-attachments/assets/e4b64e03-4cec-4e32-bae2-41c39a5d0195" />
<img width="3840" height="5114" alt="image" src="https://github.com/user-attachments/assets/37c4e51c-317c-4a80-86af-ccedf736bdc6" />

## 🛠 Server Architecture
<img width="1955" height="1540" alt="돈터치 아키텍처" src="https://github.com/user-attachments/assets/acc1630f-5eca-4a2f-aaa8-110ac448bee8" />

## 👤 Backend Developers

<table>
    <tr height="200px">
        <td align="center" width="200px">
            <a href="https://github.com/mmije0ng">
                <img height="150px" width="150px" src="https://avatars.githubusercontent.com/mmije0ng"/>
            </a>
            <br />
            <a href="https://github.com/mmije0ng">엠제이/박미정</a>
        </td>
        <td align="center" width="200px">
            <a href="https://github.com/Yujin1219">
                <img height="150px" width="150px" src="https://avatars.githubusercontent.com/Yujin1219"/>
            </a>
            <br />
            <a href="https://github.com/Yujin1219">지니/유진</a>
        </td>
        <td align="center" width="200px">
            <a href="https://github.com/leegy21">
                <img height="150px" width="150px" src="https://avatars.githubusercontent.com/leegy21"/>
            </a>
            <br />
            <a href="https://github.com/leegy21">영이/이가영</a>
        </td>
        <td align="center" width="200px">
            <a href="https://github.com/paul0755">
                <img height="150px" width="150px" src="https://avatars.githubusercontent.com/paul0755"/>
            </a>
            <br />
            <a href="https://github.com/paul0755">잔디/장예찬</a>
        </td>
    </tr>
</table>

## 🚀 Git Flow
- `main`
  - 프로젝트 최종 merge
  - 기본 프로젝트 세팅, 배포 가능한 브랜치, 항상 배포 가능한 상태를 유지
- `develop`
  - 데모데이 전까지 완성한 기능들을 계속해서 merge
  - 배포 가능한 브랜치, 항상 배포 가능한 상태를 유지
- `{type}/{issue number}`
  - 개발 브랜치
  - 예: `feat/#5`, `fix/#11`

> 작업 단위로 이슈 생성 → 브랜치 생성 → 생성한 브랜치에서 작업 후 끝나면 develop 브랜치로 PR 남기기
>
> 모든 작업 시작 전 생성한 브랜치에서 develop 브랜치 pull을 받은 후 작업

<!-- <img width="838" height="718" alt="Image" src="https://github.com/user-attachments/assets/530e9719-468e-457a-981a-e5fa46af82ff" /> -->

## 💡 PR Rules
- Assignee에는 본인을 지정해 주세요.
- Reviewers에는 본인을 제외한 백엔드 팀원 3명을 지정한 후, 카카오톡으로 공유해 주세요.
- 이후, 팀원(1명 이상)이 PR을 확인하고 승인해서 머지해 주세요.
  (해당 브랜치는 머지 후 자동 삭제되며, 복구도 가능합니다.)

## 💻 Commit Message Convention
| **Type** | **Description** |
| --- | --- |
| **Feat** | 새로운 기능 추가 |
| **Fix** | 버그 수정 |
| **Docs** | 문서 수정 |
| **Style** | 코드 formatting, 세미콜론 누락, 코드 자체의 변경이 없는 경우 |
| **Refactor** | 코드 리팩토링 |
| **Test** | 테스트 코드, 리팩토링 테스트 코드 추가 |
| **Chore** | 패키지 매니저 수정, 그 외 기타 수정 (예: .gitignore) |
| **Design** | CSS 등 사용자 UI 디자인 변경 |
| **Comment** | 필요한 주석 추가 및 변경 |
| **Rename** | 파일 또는 폴더 명을 수정하거나 옮기는 작업만인 경우 |
| **Remove** | 파일을 삭제하는 작업만 수행한 경우 |
| **Init** | 프로젝트 초기 세팅 |
| **Merge** | 브랜치 merge |
| **!BREAKING CHANGE** | 커다란 API 변경의 경우 |
| **!HOTFIX** | 급하게 치명적인 버그를 고쳐야 하는 경우 |
> [#Issue Number] Type: commit title
>
> ex. `[#5] Feat: 로그인 기능 추가`
