# Imports the Flask library and some other helper libraries.
from dataclasses import dataclass
import logging
from typing import Dict, List, Optional

from flask import Flask, redirect, request, render_template, session

from cryptography.fernet import Fernet

# Security Fixes:
# Encrypted Cookies - Encrypted cookies with Fernet so people can't just guess and get them at will.
# Cookies in /admin - /admin was accessible by everyone, fixed by adding a simple cookie check (is_admin)

# Initializes the Flask web server.
app = Flask(__name__)

key = "GhwveuAFGGDOHybR-krrNwlP0XJ3ezlYrUKG-n416jQ="
fernet = Fernet(key)

'''
This code sets up the data structures which are used to store all of the information used by the app.
'''
@dataclass
class User:
    username: str
    password: str
    balance: int
    is_admin: bool

@dataclass
class Product:
    product_id: int
    name: str
    description: str
    price: int
    image_url: str

@dataclass
class Purchase:
    user: User
    product: Product
    quantity: int

# The user database is a dictionary where the keys are usernames and the values are User structs.
user_database: Dict[str, User] = {
    'admin': User(username='admin', password='admin', balance=1000, is_admin=True),
    'test': User(username='test', password='test', balance=100, is_admin=False),
}

# The product database is a pre-populated list of every available product.
product_database: List[Product] = [
    Product(product_id=0, name='Toaster', description='It does everything! Well, it toasts. Just that, really.', price=23, image_url='toaster.jpg'),
    Product(product_id=1, name='Stapler', description='Excuse me, I believe we have what will soon be your favorite stapler!', price=12, image_url='stapler.jpg'),
    Product(product_id=2, name='One Sock', description='Have you ever lost one sock, but you can\'t replace it because they\'re only sold in pairs? Well look no further!', price=2, image_url='sock.jpg'),
    Product(product_id=3, name='Laptop', description='A perfect gift for your friend who doesn\'t have enough screens in their life.', price=800, image_url='laptop.jpg'),
    Product(product_id=4, name='Worm on a String', description='You will never find a closer confidant, a more dutiful servant, or a more loyal friend than this worm on a string.', price=1, image_url='worm_on_string.jpg'),
    Product(product_id=5, name='Grand Piano', description='At $170, this piano is a steal! Seriously, at that price it must be stolen right? Or haunted? What\'s the catch?', price=170, image_url='piano.jpg'),
    Product(product_id=6, name='Oud', description='It\'s like a guitar, except you now get confused looks when you bring it to jam night.', price=65, image_url='oud.jpg'),
    Product(product_id=7, name='Sewall Hall', description='Yep, we\'re selling the entirety of Sewall hall! Students not included. No refunds.', price=1000000, image_url='sewall_hall.jpg'),
]

# The purchase database starts empty, but will get filled as purchases are made
purchase_database: List[Purchase] = []

'''
These routes handle the main user-facing pages, including viewing products and purchasing them.
'''
@app.route("/", methods=["GET"])
def index():
    '''Displays the home page of the website.'''

    # If the user is not logged in, redirect them to the login page.
    username = get_current_user()
    if not username:
        return redirect("/login")

    balance = user_database[username].balance
    products = product_database

    return render_template("index.html", username=username, balance=balance, products=products)

@app.route("/product/<int:product_id>", methods=["GET"])
def product(product_id: int):
    '''Displays the details of a specific product.'''

    # If the user is not logged in, redirect them to the login page.
    username = get_current_user()
    if not username:
        return redirect("/login")

    user = user_database[username]
    product = product_database[product_id]

    return render_template("product.html", product=product, username=username, admin=user.is_admin)

@app.route("/purchase", methods=["POST"])
def purchase():
    '''Purchases a product.'''

    # If the user is not logged in, redirect them to the login page.
    username = get_current_user()
    if not username:
        return redirect("/login")

    product_id = request.form.get("product_id", type=int)
    # Getting from the database rather than a custom value that people can abuse
    product = get_product_by_id(product_id)
    price = product.price
    quantity = request.form.get("quantity", type=int)

    # Since Quantity is checked by a HTML Field that people can edit, people can abuse this and get quantity < 0 and gain money
    if quantity < 0:
        return render_template("error.html", error="Request has invalid fields")

    if product_id is None or price is None or quantity is None:
        return render_template("error.html", error="Request is missing required fields")

    new_balance = user_database[username].balance - (price * quantity)

    if new_balance < 0:
        return render_template("error.html", error="Cannot make purchase due to insufficient funds")
    else:
        logging.info(f"New purchase: {username} bought {quantity}x {product_id}")
        user_database[username].balance = new_balance

    purchase_record = Purchase(
        user=user_database[username],
        product=product_database[product_id],
        quantity=quantity
    )

    purchase_database.append(purchase_record)
    return render_template("purchase_success.html", username=username, purchase=purchase_record)

'''
These routes are only used by administrators.
'''
# Change the fact that non-admins can access this site (Most likely with the usage of cookies)
# Fixed
@app.route("/admin", methods=["GET"])
def admin_dashboard():
    '''Allows admins to view recent purchases.'''

    # Gets the 10 most recent purchases
    recent_purchases = purchase_database[-10:]

    # FIX: Decrypt the data and check whether or not user is admin 
    username = request.cookies.get("session_token")

    if not username:
        return render_template("error.html", error="Login Required")
    
    try:
        decrypted_data = fernet.decrypt(username.encode()).decode()
    except:
        return render_template("error.html", error="Something went wrong")
    
    user = user_database.get(decrypted_data)

    if user.is_admin == False:
        return render_template("error.html", error="This page is for admins only")
    
    return render_template("admin.html", purchases=recent_purchases)

@app.route("/update_product", methods=["POST"])
def update_product():
    '''Allows admins to change the product description.'''

    product_id = request.form.get("product_id", type=int)
    new_description = request.form.get("description")

    username = request.cookies.get("session_token")

    if not username:
        return render_template("error.html", error="Login Required")
    
    try: 
        decrypted_data = fernet.decrypt(username.encode()).decode()
    except:
        return render_template("error.html", error="Something went wrong")
    
    user = user_database.get(decrypted_data)

    if user.is_admin == False:
        return render_template("error.html", error="This page is for admins only")

    if product_id is None or new_description is None:
        return render_template("error.html", error="Request is missing required fields")

    product_database[product_id].description = new_description

    return redirect(f"/product/{product_id}")

'''
These routes handle logging in, creating accounts, and determining who is currently logged in.
'''
@app.route("/login", methods=["GET"])
def login_get():
    '''Return the login page of the website.'''

    return render_template("login.html")

# Encryption Fixed
@app.route("/login", methods=["POST"])
def login_post():
    '''Logs the user in, if they supply the correct password.'''

    username = request.form.get("username")
    password = request.form.get("password")
    if username is None or password is None:
        return render_template("error.html", error="Username and password are both required")
    
    user = user_database.get(username)
    
    if user is None:
        return render_template("error.html", error="User does not exist")

    if password == user.password:
        # If the password is correct, set the session cookie and send them back to the home page
        resp = redirect("/")
        encrypted = fernet.encrypt(username.encode()).decode()
        resp.set_cookie("session_token", encrypted)
        return resp
    else:
        return render_template("error.html", error="Incorrect password")


@app.route("/create_account", methods=["GET"])
def create_account_get():
    '''Return the create_account page of the website.'''

    return render_template("create_account.html")

@app.route("/create_account", methods=["POST"])
def create_account_post():
    '''Creates a new account.'''

    username = request.form.get("username")
    password = request.form.get("password")
    if username is None or password is None:
        return render_template("error.html", error="Username and password are both required")

    if username in user_database:
        return render_template("error.html", error="A user with that username already exists")

    user_database[username] = User(
        username=username,
        password=password,
        balance=100,
        is_admin=False
    )

    # Log in as the newly created user.
    # FIX: Encrypted cookie
    resp = redirect("/")
    encrypted = fernet.encrypt(username.encode()).decode()
    resp.set_cookie("session_token", encrypted)
    return resp

@app.route("/logout", methods=["GET"])
def logout():
    '''Logs the user out.'''

    resp = redirect("/")
    resp.delete_cookie("session_token")
    return resp

def get_current_user() -> Optional[str]:
    '''Return the current logged-in user if they exist, otherwise return None.'''

    if "session_token" in request.cookies:
        username = request.cookies.get("session_token")
        decrypted_data = fernet.decrypt(username.encode()).decode()
        return decrypted_data
    else:
        return None

def get_product_by_id(product_id: int) -> Optional[Product]:
    for product in product_database:
        if product.product_id == product_id:
            return product
    return None

# Run the app
app.run(debug=True, port=8000)