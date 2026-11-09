
#!/bin/bash
set -e

cd "$(dirname "$0")"

echo "Building React frontend..."
cd frontend
npm install
npm run build

echo "Copying frontend to Spring Boot..."
cd ..

mkdir -p backend/src/main/resources/static
rm -rf backend/src/main/resources/static/assets
rm -f backend/src/main/resources/static/index.html
cp -R frontend/dist/. backend/src/main/resources/static/

echo "Building Spring Boot backend..."
cd backend
./mvnw clean package

echo "Full-stack build completed successfully!"
