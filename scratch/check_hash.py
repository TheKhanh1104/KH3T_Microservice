import bcrypt

hashes = {
    "Leesin": b"$2a$10$pdErrGmqR6k4c2cHmTVrCOoKtQmoR.frS.lAFbvU6e7/Cjbnt98Xi",
    "Halland": b"$2a$10$UwU6c/qJC6Tg9/ySe5RYLOCtH3pTHzakrVAV0hjRfWzNVCe2kyJni",
    "Doku": b"$2a$10$ezcfId8HGRycvLNNEQZdG.hLaSJ4xLvNoi0KRUkBU6tgu6vlKN2n2",
}

passwords = [
    b"admin",
    b"123456",
    b"leesin123",
    b"leesin",
    b"halland",
    b"halland123",
    b"123",
    b"1234",
    b"12345",
    b"12345678",
]

for name, h in hashes.items():
    found = False
    for p in passwords:
        if bcrypt.checkpw(p, h):
            print(f"MATCH FOUND for {name}: Password is '{p.decode()}'")
            found = True
            break
    if not found:
        # Try checking if password is lowercase name
        p_name = name.lower().encode()
        if bcrypt.checkpw(p_name, h):
            print(f"MATCH FOUND for {name}: Password is '{p_name.decode()}'")
            continue
        p_name_123 = (name.lower() + "123").encode()
        if bcrypt.checkpw(p_name_123, h):
            print(f"MATCH FOUND for {name}: Password is '{p_name_123.decode()}'")
            continue
        print(f"No match for {name}")
