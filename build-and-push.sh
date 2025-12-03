#!/bin/bash
# 라즈베리파이에서 로컬 빌드 및 푸시 스크립트

set -e

# 설정
REGISTRY="ghcr.io"
REPO_NAME=$(echo "$1" | tr '[:upper:]' '[:lower:]')
TAG="${2:-latest}"
IMAGE_NAME="${REGISTRY}/${REPO_NAME}:${TAG}"

echo "🔨 Docker 이미지 빌드 시작..."
echo "이미지: ${IMAGE_NAME}"

# BuildKit 활성화
export DOCKER_BUILDKIT=1

# 빌드 (ARM64 네이티브)
time docker build \
  --platform linux/arm64 \
  --build-arg BUILDKIT_INLINE_CACHE=1 \
  --cache-from ${IMAGE_NAME} \
  --tag ${IMAGE_NAME} \
  --progress=plain \
  .

echo "✅ 빌드 완료!"
echo "📦 이미지 크기:"
docker images ${IMAGE_NAME}

# GitHub Container Registry에 푸시
if [ "$3" == "push" ]; then
  echo "📤 이미지 푸시 중..."
  docker push ${IMAGE_NAME}
  echo "✅ 푸시 완료!"
fi

echo ""
echo "사용법:"
echo "  로컬 빌드만:      ./build-and-push.sh owner/repo"
echo "  빌드 및 푸시:     ./build-and-push.sh owner/repo latest push"
