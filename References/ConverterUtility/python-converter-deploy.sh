IMAGE_NAME="converter-image"
IMAGE_TAG="latest"
GCR_REGION="us-central1"
GCR_PROJECT="stellar-stream-452907-k9"
GCR_REPOSITORY="python-converter-registry"
DOCKERFILE_PATH="."

docker build -t ${GCR_REGION}-docker.pkg.dev/${GCR_PROJECT}/${GCR_REPOSITORY}/${IMAGE_NAME}:${IMAGE_TAG} ${DOCKERFILE_PATH}

docker push ${GCR_REGION}-docker.pkg.dev/${GCR_PROJECT}/${GCR_REPOSITORY}/${IMAGE_NAME}:${IMAGE_TAG}

kubectl apply -f python-converter.yaml

kubectl rollout restart deployment converter-deployment -n default

kubectl rollout status deployment $NAME -n default