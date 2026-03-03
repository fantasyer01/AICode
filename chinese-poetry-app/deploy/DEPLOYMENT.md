# 本地执行打包命令

powershell -ExecutionPolicy Bypass -File  .\deploy\package-for-deployment.ps1

# 服务器端解压缩

unzip chinese-poetry-app.zip -d /home/lighthouse/chinese-poetry-app

# 服务器端服务重启

docker-compose build
docker-compose up -d