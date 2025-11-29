#!/bin/bash

# EOD 애플리케이션 Docker Compose 배포 스크립트
# 사용법: ./scripts/deploy.sh

set -e  # 에러 발생 시 스크립트 중단

# 색상 출력
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}   EOD 애플리케이션 배포 시작${NC}"
echo -e "${BLUE}   (Docker Compose 방식)${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# 현재 디렉토리 확인
if [ ! -f "docker-compose.prod.yml" ]; then
    echo -e "${RED}❌ docker-compose.prod.yml 파일을 찾을 수 없습니다.${NC}"
    echo "프로젝트 루트 디렉토리에서 실행하세요."
    exit 1
fi

# .env 파일 확인
echo -e "${GREEN}[1/5] 환경변수 확인 중...${NC}"
if [ ! -f ".env" ]; then
    echo -e "${YELLOW}⚠️  .env 파일이 없습니다. .env.example을 복사하여 생성합니다.${NC}"
    
    if [ -f ".env.example" ]; then
        cp .env.example .env
        echo -e "${YELLOW}📝 .env 파일이 생성되었습니다. 환경변수를 수정하세요!${NC}"
        echo ""
        echo -e "${RED}필수 수정 항목:${NC}"
        echo "  - GITHUB_REPOSITORY"
        echo "  - SPRING_DATASOURCE_PASSWORD"
        echo "  - MYSQL_ROOT_PASSWORD"
        echo ""
        echo "편집 명령어: nano .env 또는 vi .env"
        exit 1
    else
        echo -e "${RED}❌ .env.example 파일도 없습니다.${NC}"
        exit 1
    fi
fi

# Docker 설치 확인
echo -e "${GREEN}[2/5] Docker 환경 확인 중...${NC}"
if ! command -v docker &> /dev/null; then
    echo -e "${RED}❌ Docker가 설치되어 있지 않습니다.${NC}"
    echo "Docker 설치: curl -fsSL https://get.docker.com | sh"
    exit 1
fi

if ! command -v docker-compose &> /dev/null; then
    echo -e "${RED}❌ Docker Compose가 설치되어 있지 않습니다.${NC}"
    exit 1
fi

# GitHub Container Registry 로그인 확인
echo -e "${GREEN}[3/5] GitHub Container Registry 로그인 확인...${NC}"
GITHUB_REPOSITORY=$(grep GITHUB_REPOSITORY .env | cut -d '=' -f2)

if [ -z "$GITHUB_REPOSITORY" ]; then
    echo -e "${RED}❌ .env 파일에 GITHUB_REPOSITORY가 설정되지 않았습니다.${NC}"
    exit 1
fi

# 최신 이미지 Pull 및 배포
echo -e "${GREEN}[4/5] Docker Compose로 서비스 시작 중...${NC}"
echo "이미지: ghcr.io/${GITHUB_REPOSITORY}:latest"
echo ""

# 이미지 pull
docker-compose -f docker-compose.prod.yml pull

# 서비스 시작 (detached mode)
docker-compose -f docker-compose.prod.yml up -d

# 배포 확인
echo -e "${GREEN}[5/5] 배포 상태 확인 중...${NC}"
sleep 5

echo ""
echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}   컨테이너 실행 상태${NC}"
echo -e "${BLUE}========================================${NC}"
docker-compose -f docker-compose.prod.yml ps

# 헬스체크
echo ""
echo -e "${GREEN}MySQL 헬스체크 중...${NC}"
for i in {1..30}; do
    if docker-compose -f docker-compose.prod.yml exec -T mysql mysqladmin ping -h localhost --silent; then
        echo -e "${GREEN}✅ MySQL 준비 완료${NC}"
        break
    fi
    if [ $i -eq 30 ]; then
        echo -e "${RED}⚠️  MySQL이 30초 내에 준비되지 않았습니다.${NC}"
    fi
    sleep 1
done

# 애플리케이션 로그 확인
echo ""
echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}   애플리케이션 로그 (최근 20줄)${NC}"
echo -e "${BLUE}========================================${NC}"
docker-compose -f docker-compose.prod.yml logs --tail=20 app

# 사용하지 않는 이미지 정리
echo ""
echo -e "${BLUE}사용하지 않는 이미지 정리 중...${NC}"
docker image prune -af

echo ""
echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}   ✅ 배포 완료!${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""
echo "📋 유용한 명령어:"
echo "  전체 로그 확인:      docker-compose -f docker-compose.prod.yml logs -f"
echo "  앱 로그만 확인:      docker-compose -f docker-compose.prod.yml logs -f app"
echo "  MySQL 로그 확인:     docker-compose -f docker-compose.prod.yml logs -f mysql"
echo "  서비스 상태 확인:    docker-compose -f docker-compose.prod.yml ps"
echo "  서비스 중지:         docker-compose -f docker-compose.prod.yml down"
echo "  서비스 재시작:       docker-compose -f docker-compose.prod.yml restart"
echo ""
echo "🌐 애플리케이션 접속: http://localhost:8080"
echo "📚 API 문서:          http://localhost:8080/swagger-ui.html"
echo ""
