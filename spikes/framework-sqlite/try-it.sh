#!/bin/sh
# Usage: ./try-it.sh 7070   (Javalin)   or   ./try-it.sh 7071   (plain JDK)
P=${1:-7070}; U=http://localhost:$P/api/chores
echo "1. list:";            curl -s $U; echo
echo "2. add a chore:";     curl -s -i -X POST $U -d '{"title":"Take out recycling","assigneeUserId":2,"dueDate":"2026-11-10"}' | sed -n '1p;$p'; echo
echo "3. title with quotes:"; curl -s -X POST $U -d '{"title":"Clean \"bathroom\"","assigneeUserId":3,"dueDate":"2026-11-12"}'; echo
echo "4. bad date:";        curl -s -i -X POST $U -d '{"title":"x","assigneeUserId":2,"dueDate":"tomorrow"}' | sed -n '1p;$p'; echo
echo "5. missing assignee:"; curl -s -i -X POST $U -d '{"title":"x","dueDate":"2026-11-10"}' | sed -n '1p;$p'; echo
echo "6. SQL injection attempt:"; curl -s -X POST $U -d "{\"title\":\"x'); DROP TABLE chore;--\",\"assigneeUserId\":2,\"dueDate\":\"2026-11-11\"}"; echo
echo "7. list again:";      curl -s $U; echo
