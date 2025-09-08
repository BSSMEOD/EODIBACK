name: "🐞 버그 리포트"
description: "버그가 발생했나요?"
title: "[버그] "
labels: "버그"
body:
- type: textarea
  attributes:
  label: 🐞설명
  description: 버그에 대한 설명을 작성해 주세요.
  placeholder: 꼼꼼하게 적을수록 좋습니다!
  validations:
  required: true
- type: textarea
  attributes:
  label: ⛰️ 문제 상황
  description: 버그가 발생한 맥락(=재현 방법)을 작성해 주세요.
  placeholder: |
  1. 서버를 실행한다.
  2. "DB 연결 실패" 라는 메세지가 뜬다.
  validations:
  required: true
- type: textarea
  attributes:
  label: 🌏 해결
  description: 버그를 해결할 방법에 대해 작성해 주세요.
  placeholder: |
  docker-compose.yml 파일에 DB 접속 포트 번호 바꾸기
  validations:
  required: true
- type: textarea
  attributes:
  label: 📄 에러 메세지
  description: 에러 메세지가 있다면 복붙해 주세요.
  render: shell
  validations:
  required: false
- type: textarea
  attributes:
  label: 🙋🏻 덧붙일 말
  description: 더 하고 싶은 말이 있다면 작성해 주세요.