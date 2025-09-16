# MotionSource

An Android app that streams device orientation as quaternions over UDP.

## Features
- Streams orientation quaternions `(w, x, y, z)` via UDP at a selectable rate
- Foreground service with status notification and play/pause control
- Simple UI to set target `IP`, `Port`, and polling rate
- Built with Kotlin, Jetpack Compose, and Coroutines

## How it works
- `Sensor.TYPE_ROTATION_VECTOR` is read by `DeviceRotationSensor` and converted to a quaternion.
- `OrientationAngleService` runs in the foreground, samples the sensor, and sends packets via `UdpSender`.
- Each packet is 16 bytes: 4 little‑endian IEEE‑754 floats in order `w, x, y, z`.

## App UI
- Enter target `IP` and `Port`.
- Choose polling rate with the slider: 125 Hz, 250 Hz, 500 Hz, 1000 Hz.
- Tap "Start Server" to start streaming; the button toggles to pause/resume.
- When running, the current quaternion is displayed.

## Permissions
Declared in `AndroidManifest.xml`:
- `INTERNET`
- `ACCESS_WIFI_STATE`
- `ACCESS_NETWORK_STATE`
- `FOREGROUND_SERVICE` (+ `FOREGROUND_SERVICE_DATA_SYNC` on newer Android)
- `POST_NOTIFICATIONS` (for the foreground service notification)
- `HIGH_SAMPLING_RATE_SENSORS`

## Build and Run
1. Open the project in Android Studio (Giraffe or newer recommended).
2. Use the included Gradle wrapper; no extra setup needed.
3. Select a device (a real device recommended for sensors) and Run.

Alternatively from terminal:
```
./gradlew assembleDebug
```
The APK will be in `app/build/outputs/apk/debug/`.

## Usage
1. Ensure your phone and receiver machine are on the same network.
2. On the receiver, open a UDP socket on the chosen port.
3. In the app, enter the receiver's IP and port, set the poll rate, then Start.

## UDP Packet Format
- Transport: UDP
- Payload size: 16 bytes
- Endianness: little‑endian
- Layout: `float32 w`, `float32 x`, `float32 y`, `float32 z`

### Example: Python receiver
```python
import socket, struct

IP = "0.0.0.0"     # listen on all interfaces
PORT = 42069        # match the app's port

sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
sock.bind((IP, PORT))
print(f"Listening on {IP}:{PORT} ...")

while True:
    data, addr = sock.recvfrom(1024)
    if len(data) < 16:
        continue
    w, x, y, z = struct.unpack('<ffff', data[:16])
    print(f"from {addr}: w={w:+.6f}, x={x:+.6f}, y={y:+.6f}, z={z:+.6f}")
```

### Example: Node.js receiver
```javascript
const dgram = require('dgram');
const server = dgram.createSocket('udp4');

server.on('message', (msg, rinfo) => {
  if (msg.length >= 16) {
    const w = msg.readFloatLE(0);
    const x = msg.readFloatLE(4);
    const y = msg.readFloatLE(8);
    const z = msg.readFloatLE(12);
    console.log(`from ${rinfo.address}:${rinfo.port} -> w=${w.toFixed(6)} x=${x.toFixed(6)} y=${y.toFixed(6)} z=${z.toFixed(6)}`);
  }
});

server.bind(42069, () => console.log('Listening on 0.0.0.0:42069'));
```

## Key Modules
- `com.example.motionsource.MainActivity` – Compose UI and controls
- `com.example.motionsource.services.OrientationAngleService` – foreground service, sensor loop, UDP send
- `com.example.motionsource.sensors.DeviceRotationSensor` – sensor access and quaternion production
- `com.example.motionsource.udpsender.UdpSender` – UDP socket send wrapper

## Troubleshooting
- No data arriving: verify phone and receiver are on the same subnet and the port is open on the receiver.
- High packet loss: reduce poll rate (e.g., 125–250 Hz) or prefer 5 GHz Wi‑Fi.
- App killed in background: the service runs foreground, but device OEM power optimizations may still throttle; exclude the app from battery optimizations if needed.

## Roadmap / Ideas
- Optional TCP/websocket mode with reconnects
- Timestamped packets and sequence numbers
- Calibration and coordinate‑frame options
- Auto‑discovery of receiver via broadcast

## License
This project is licensed under the MIT License. See the `LICENSE` file for details.
