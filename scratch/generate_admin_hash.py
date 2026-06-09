import bcrypt

password = b"admin"
hashed = bcrypt.hashpw(password, bcrypt.gensalt(10))
print(hashed.decode())
