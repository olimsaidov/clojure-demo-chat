var input = document.getElementById('input');
var output = document.getElementById('output');
var send = document.getElementById('send');

var ws = new WebSocket((window.location + '')
  .replace(/^http/, 'ws')
  .replace('/chat', '/websocket'));

send.onclick = function() {
  ws.send(input.value);
  input.value = '';
  input.focus();
};

input.onkeydown = function (e) {
  if (e.keyCode === 13 && e.ctrlKey) {
    ws.send(input.value);
    input.value = '';
    input.focus();
  }
};

ws.onmessage = function(e) {
  console.log(e.data);

  var scrolledDown =
    (output.clientHeight + output.scrollTop) ===
    output.scrollHeight;

  output.innerHTML += e.data;

  if (scrolledDown)
    output.scrollTop = output.scrollHeight;
};
