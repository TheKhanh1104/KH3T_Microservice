import os
import subprocess
import sys

# Ensure pymysql is installed
try:
    import pymysql
except ImportError:
    print("pymysql is not installed. Installing it now...")
    subprocess.check_call([sys.executable, "-m", "pip", "install", "pymysql"])
    import pymysql

# Connection details from render.yaml
HOST = "hv-sgp1-003.clvrcld.net"
PORT = 14700
USER = "u0gfjpm1l8qfzgvi"
PASSWORD = "Hy43lVMta7O1auqFzNJS"
DATABASE = "bdn7ruaxqdmz1bhougtm"

def run_sql_file(connection, filepath):
    print(f"Reading: {filepath}")
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()
    
    # Robust char-by-char SQL splitter to handle semicolons inside quotes correctly
    statements = []
    current = []
    in_quote = None  # Can be "'" or '"'
    escaped = False
    
    for char in content:
        if escaped:
            current.append(char)
            escaped = False
            continue
            
        if char == '\\':
            current.append(char)
            escaped = True
            continue
            
        if in_quote:
            current.append(char)
            if char == in_quote:
                in_quote = None
            continue
            
        if char in ("'", '"'):
            in_quote = char
            current.append(char)
            continue
            
        if char == ';':
            statements.append(''.join(current).strip())
            current = []
            continue
            
        current.append(char)
        
    if current:
        last = ''.join(current).strip()
        if last:
            statements.append(last)
            
    with connection.cursor() as cursor:
        for i, stmt in enumerate(statements, 1):
            # Strip comments from statement
            lines = []
            for line in stmt.splitlines():
                l = line.strip()
                if l.startswith('--') or l.startswith('/*'):
                    continue
                lines.append(line)
            clean_stmt = '\n'.join(lines).strip()
            if clean_stmt:
                try:
                    cursor.execute(clean_stmt)
                except Exception as e:
                    print(f"Error executing statement #{i}: {clean_stmt[:150]}...")
                    print(f"Error message: {e}")
                    connection.rollback()
                    raise e
    connection.commit()
    print(f"Successfully executed statements from {filepath}")

def main():
    print("Connecting to Clever Cloud MySQL...")
    try:
        connection = pymysql.connect(
            host=HOST,
            port=PORT,
            user=USER,
            password=PASSWORD,
            database=DATABASE,
            charset='utf8mb4',
            cursorclass=pymysql.cursors.DictCursor
        )
        print("Connected successfully!")
    except Exception as e:
        print(f"Failed to connect to database: {e}")
        return

    try:
        # Step 1: Run reset_data.sql
        reset_path = os.path.join("kh3tshop-be", "scripts", "reset_data.sql")
        run_sql_file(connection, reset_path)
        print("Cleared old data successfully!")
        
        # Step 2: Run JPA.sql
        jpa_path = os.path.join("kh3tshop-be", "scripts", "JPA.sql")
        run_sql_file(connection, jpa_path)
        print("Imported new data successfully!")
        
        print("\nDATABASE RESET AND IMPORT COMPLETED SUCCESSFULLY ON CLEVER CLOUD!")
    except Exception as e:
        print(f"\nFailed to complete database operation: {e}")
    finally:
        connection.close()

if __name__ == "__main__":
    main()
