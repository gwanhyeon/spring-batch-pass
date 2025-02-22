# 백그라운드 실행, 강제 재생성
db-up:
	docker-compose up -d --force-recreate

db-down:
	docker-compose down -v
