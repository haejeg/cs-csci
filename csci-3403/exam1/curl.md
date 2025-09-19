curl ^"http://127.0.0.1:8000/purchase^" ^
  -H ^"Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7^" ^
  -H ^"Accept-Language: en-US,en;q=0.9,ko-KR;q=0.8,ko;q=0.7^" ^
  -H ^"Cache-Control: max-age=0^" ^
  -H ^"Connection: keep-alive^" ^
  -H ^"Content-Type: application/x-www-form-urlencoded^" ^
  -b ^"session_token=gAAAAABozOUAyQ6j785_ada_NJ_xx0b1RAR6H2z8yrpyB5--GKOi47-wBBLY_KN5qljDKVBObTeWHcRoVl5-bbLvSJuqM1hn5g==^" ^
  -H ^"Origin: http://127.0.0.1:8000^" ^
  -H ^"Referer: http://127.0.0.1:8000/product/1^" ^
  -H ^"Sec-Fetch-Dest: document^" ^
  -H ^"Sec-Fetch-Mode: navigate^" ^
  -H ^"Sec-Fetch-Site: same-origin^" ^
  -H ^"Sec-Fetch-User: ?1^" ^
  -H ^"Upgrade-Insecure-Requests: 1^" ^
  -H ^"User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/140.0.0.0 Safari/537.36^" ^
  -H ^"sec-ch-ua: ^\^"Chromium^\^";v=^\^"140^\^", ^\^"Not=A?Brand^\^";v=^\^"24^\^", ^\^"Google Chrome^\^";v=^\^"140^\^"^" ^
  -H ^"sec-ch-ua-mobile: ?0^" ^
  -H ^"sec-ch-ua-platform: ^\^"Windows^\^"^" ^
  --data-raw ^"product_id=1^&price=12^&quantity=-100^"


"REQUEST HAS INVALID FIELDS"