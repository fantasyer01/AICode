# 本地执行打包命令

powershell -ExecutionPolicy Bypass -File  .\deploy\package-for-deployment.ps1

# 服务器端解压缩

unzip -O UTF-8 chinese-poetry-app.zip -d /home/app/chinese-poetry-app

# 服务器端服务重启

docker compose build
docker compose up -d

# 一键更新部署
powershell -ExecutionPolicy Bypass -File .\deploy\deploy.ps1