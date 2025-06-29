# HTML Test Client Deployment

Simple Python script to deploy and serve the WebSocket test HTML client.

## Quick Start

1. **Start your Spring Boot WebSocket server** (if not already running):
   ```bash
   mvn spring-boot:run
   ```

2. **Deploy the HTML test client**:
   ```bash
   python deploy_html.py
   ```

3. **Browser automatically opens** to: `http://localhost:3000/websocket-test.html`

4. **Start testing** - Click "Connect" in the web interface

## Usage Options

### Basic Usage
```bash
# Default: serves on localhost:3000, auto-opens browser
python deploy_html.py
```

### Custom Port
```bash
# Use different port
python deploy_html.py --port 8888
```

### Custom Host
```bash
# Bind to all interfaces (accessible from other machines)
python deploy_html.py --host 0.0.0.0 --port 3000
```

### No Auto-Browser
```bash
# Don't automatically open browser
python deploy_html.py --no-browser
```

## How It Works

1. **HTTP Server**: Uses Python's built-in `http.server` to serve the HTML file
2. **Auto-Browser**: Automatically opens your default browser to the test client
3. **WebSocket Connection**: The HTML client connects to your Spring Boot server at `ws://localhost:8080/ws`
4. **Clean Shutdown**: Press `Ctrl+C` to stop the server gracefully

## Troubleshooting

### Port Already in Use
```
Error: Port 3000 is already in use
Try a different port: python deploy_html.py --port 3001
```

**Solution**: Use a different port or kill the process using port 3000.

### HTML File Not Found
```
Error: websocket-test.html not found in current directory
```

**Solution**: Run the script from the directory containing `websocket-test.html`.

### WebSocket Connection Failed
- **Check**: Spring Boot server is running on port 8080
- **Check**: No firewall blocking connections
- **Check**: Browser console for error messages

### Browser Doesn't Open
- **Manual**: Navigate to `http://localhost:3000/websocket-test.html`
- **Try**: Different browser
- **Use**: `--no-browser` flag and open manually

## Network Access

### Local Only (Default)
```bash
python deploy_html.py
# Accessible only from same machine
```

### Network Access
```bash
python deploy_html.py --host 0.0.0.0
# Accessible from other machines on network
# Access via: http://YOUR_IP:3000/websocket-test.html
```

## Example Session

```bash
$ python deploy_html.py
Starting HTTP server...
Serving websocket-test.html at: http://localhost:3000/websocket-test.html
WebSocket will connect to: ws://localhost:8080/ws
Press Ctrl+C to stop the server
--------------------------------------------------
Opening browser to: http://localhost:3000/websocket-test.html

# Browser opens automatically
# Click "Connect" in the web interface
# Start testing WebSocket functionality

^C
Shutting down server...
```

## Integration with Development

### During Development
1. Start Spring Boot: `mvn spring-boot:run` (Terminal 1)
2. Start HTML client: `python deploy_html.py` (Terminal 2)
3. Develop and test
4. Restart either service as needed

### For Demos
1. One command deployment: `python deploy_html.py`
2. Share URL with team: `http://localhost:3000/websocket-test.html`
3. Professional presentation without manual file opening

## Requirements

- **Python 3.6+** (uses built-in libraries only)
- **websocket-test.html** in current directory
- **Spring Boot WebSocket server** running on port 8080 (for WebSocket functionality)

No additional Python packages required!