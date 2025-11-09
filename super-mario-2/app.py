from flask import Flask, render_template

app = Flask(__name__)

@app.route('/')
def index():
    """Redirect to game page"""
    return render_template('game.html')

if __name__ == '__main__':
    print("=" * 60)
    print("🎮 Super Mario Game Starting...")
    print("=" * 60)
    print("📍 Game URL: http://localhost:5000")
    print("=" * 60)
    
    app.run(debug=True, host='0.0.0.0', port=5000)