# 本地执行打包命令

powershell -ExecutionPolicy Bypass -File  .\deploy\package-for-deployment.ps1

# 服务器端解压缩

unzip chinese-poetry-app.zip -d /home/lighthouse/chinese-poetry-app

# 服务器端服务重启

sudo systemctl restart chinese-poetry
sudo systemctl status chinese-poetry

# Apache服务器配置检查及重新加载

sudo /usr/local/lighthouse/softwares/apache/bin/apachectl configtest
sudo /usr/local/lighthouse/softwares/apache/bin/apachectl graceful