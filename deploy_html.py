#!/usr/bin/env python3
"""
Simple HTTP server to deploy and serve the WebSocket test HTML client.
Usage: python deploy_html.py [--port PORT] [--host HOST]
"""

import argparse
import http.server
import socketserver
import webbrowser
import os
import sys
import signal
import threading
import time
import json
from datetime import datetime

class EnhancedHTTPRequestHandler(http.server.SimpleHTTPRequestHandler):
    """Enhanced HTTP request handler with better logging"""
    
    def log_message(self, format, *args):
        # Log with timestamp and better formatting
        timestamp = datetime.now().strftime("%H:%M:%S")
        if args and len(args) >= 2:
            method = args[0] if args[0] else "GET"
            status = args[1] if args[1] else "200"
            path = args[2] if len(args) > 2 else "/"
            
            if '200' in str(status):
                print(f"[{timestamp}] ✓ {method} {path} - {status}")
            else:
                print(f"[{timestamp}] ✗ {method} {path} - {status}")
        else:
            super().log_message(format, *args)
    
    def end_headers(self):
        # Add CORS headers for WebSocket testing
        self.send_header('Access-Control-Allow-Origin', '*')
        self.send_header('Access-Control-Allow-Methods', 'GET, POST, OPTIONS')
        self.send_header('Access-Control-Allow-Headers', 'Content-Type')
        super().end_headers()

def signal_handler(signum, frame):
    """Handle Ctrl+C gracefully"""
    print("\n\nShutting down server...")
    sys.exit(0)

def open_browser_delayed(url, delay=1.5):
    """Open browser after a short delay to ensure server is ready"""
    def delayed_open():
        time.sleep(delay)
        print(f"🌐 Opening browser to: {url}")
        webbrowser.open(url)
    
    thread = threading.Thread(target=delayed_open)
    thread.daemon = True
    thread.start()

def print_usage_instructions():
    """Print helpful usage instructions"""
    print("\n📋 WebSocket Test Instructions:")
    print("   1. Click 'Connect' to establish WebSocket connection")
    print("   2. Use 'Send Ping' to test basic connectivity")
    print("   3. Use 'Calculate Fee' to test EOS fee calculation")
    print("   4. Use 'Test Rate Limit' to verify 20 requests/minute limit")
    print("   5. Watch this terminal for formatted message logs")
    print("\n💡 Tips:")
    print("   - Messages are logged here in readable format")
    print("   - Sent messages show as [SENT] with ➡️")
    print("   - Received messages show as [RECV] with ⬅️")
    print("   - Errors and rate limits are highlighted")
    print()

def main():
    parser = argparse.ArgumentParser(description='Deploy WebSocket HTML test client')
    parser.add_argument('--port', type=int, default=3000, 
                       help='Port to serve on (default: 3000)')
    parser.add_argument('--host', default='localhost', 
                       help='Host to bind to (default: localhost)')
    parser.add_argument('--no-browser', action='store_true',
                       help='Do not auto-open browser')
    parser.add_argument('--verbose', action='store_true',
                       help='Enable verbose HTTP request logging')
    
    args = parser.parse_args()
    
    # Check if HTML file exists
    html_file = 'websocket-test.html'
    if not os.path.exists(html_file):
        print(f"Error: {html_file} not found in current directory")
        print("Please run this script from the directory containing websocket-test.html")
        sys.exit(1)
    
    # Set up signal handler for graceful shutdown
    signal.signal(signal.SIGINT, signal_handler)
    
    # Choose request handler based on verbose flag
    handler_class = EnhancedHTTPRequestHandler
    
    # Create HTTP server
    try:
        with socketserver.TCPServer((args.host, args.port), handler_class) as httpd:
            url = f"http://{args.host}:{args.port}/{html_file}"
            
            print(f"🚀 Starting HTTP server...")
            print(f"📄 Serving {html_file} at: {url}")
            print(f"🔌 WebSocket will connect to: ws://localhost:8080/ws")
            print(f"⏹️  Press Ctrl+C to stop the server")
            print("=" * 60)
            
            # Auto-open browser unless disabled
            if not args.no_browser:
                open_browser_delayed(url)
                
            # Print usage instructions
            print_usage_instructions()
            
            print("🎯 Server is ready! Waiting for connections...")
            print("=" * 60)
            
            # Start serving
            httpd.serve_forever()
            
    except OSError as e:
        if e.errno == 48 or e.errno == 10048:  # Address already in use
            print(f"Error: Port {args.port} is already in use")
            print(f"Try a different port: python deploy_html.py --port {args.port + 1}")
        else:
            print(f"Error starting server: {e}")
        sys.exit(1)
    except Exception as e:
        print(f"Unexpected error: {e}")
        sys.exit(1)

if __name__ == '__main__':
    main()