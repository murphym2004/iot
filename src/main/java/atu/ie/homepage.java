String homePagePart1 = F(R"=====(<!DOCTYPE html>
        <html lang="en">
        <head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1, viewport-fit=cover">
<title>Servo Control WebServer</title>
<style>
body { background-color: DodgerBlue; }
.flex-Container{ display: flex; flex-direction: column; align-items: center; }
h1{ font-size: 40px; font-family: Arial; color: navy; text-align: center; }
p{ font-size: 25px; font-family: Arial; color: navy; text-align: center; }
table { font-size: 25px; padding: 8px; text-align: left; border-bottom: 1px solid #ddd; }
</style>
</head>
<body>
<div class="flex-Container">
<h1>Servo Control Website</h1>
<p>Welcome to my website where you can control the servo.</p>
<p>RFID Card ID: {{RFID_ID}}</p>  <!-- Place for dynamic RFID ID -->
<form action="/turnon" method="get">
  <button type="submit">Turn On Servo</button>
</form>
</div>
</body>
</html>)=====");
