<template>
  <div id="terminal" class="terminal-container" />
</template>

<script setup>
  import { onMounted } from 'vue'
  import { Terminal } from 'xterm'
  import 'xterm/css/xterm.css'
  import { useUserStore } from '@/stores/user'

  const userStore = useUserStore()

  let term
  let commandBuffer = ''
  const commandHistory = []
  let historyIndex = 0

  const prompt = () => {
    const name = userStore.username || 'nologin'
    const color = name === 'nologin' ? '\x1b[1;3;95m' : '\x1b[1;3;94m'
    return ` ${color}${name}\x1b[0m@notej:# ~ `
  }

  const userColor = '\x1b[38;2;0;123;255m'
  const resetColor = '\x1b[0m'

  function printAsciiArt (term) {
    const lines = [
      String.raw` $ ▐▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▌`,
      String.raw` $ ▐                                                                                   ▌`,
      String.raw` $ ▐              _ _         __    __           _     _   _                           ▌`,
      String.raw` $ ▐    /\  /\___| | | ___   / / /\ \ \___  _ __| | __| | / \                          ▌`,
      String.raw` $ ▐   / /_/ / _ \ | |/ _ \  \ \/  \/ / _ \| '__| |/ _\` |/  /                         ▌`,
      String.raw` $ ▐  / __  /  __/ | | (_) |  \  /\  / (_) | |  | | (_| /\_/                           ▌`,
      String.raw` $ ▐  \/ /_/ \___|_|_|\___/    \/  \/ \___/|_|  |_|\__,_\/                             ▌`,
      String.raw` $ ▐                                                                                   ▌`,
      String.raw` $ ▐   __    __     _                            _              __      _        __    ▌`,
      String.raw` $ ▐  / / /\ \ \___| | ___ ___  _ __ ___   ___  | |_ ___     /\ \ \___ | |_ ___  \ \   ▌`,
      String.raw` $ ▐  \ \/  \/ / _ \ |/ __/ _ \| '_ \` _ \ / _ \ | __/ _ \   /  \/ / _ \| __/ _ \  \ \ ▌`,
      String.raw` $ ▐   \  /\  /  __/ | (_| (_) | | | | | |  __/ | || (_) | / /\  / (_) | ||  __/\_/ /  ▌`,
      String.raw` $ ▐    \/  \/ \___|_|\___\___/|_| |_| |_|\___|  \__\___/  \_\ \/ \___/ \__\___\___/   ▌`,
      String.raw` $ ▐                                                                                   ▌`,
      String.raw` $ ▐▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▌`,
      String.raw` $  `,
    ]

    lines.forEach(line => {
      const leftIndex = line.indexOf('▐')
      const rightIndex = line.lastIndexOf('▌')

      if (leftIndex !== -1 && rightIndex !== -1 && rightIndex > leftIndex) {
        const left = line.substring(0, leftIndex + 1)
        const middle = line.substring(leftIndex + 1, rightIndex)
        const right = line.substring(rightIndex)

        if (/^[\s▀▄]+$/.test(middle)) {
          term.writeln(line)
        } else {
          term.writeln(left + userColor + middle + resetColor + right)
        }
      } else {
        term.writeln(line)
      }
    })
  }

  function redrawCommandBuffer () {
    term.write('\x1B[2K\r' + prompt() + userColor + commandBuffer + resetColor)
  }

  function processCommand (cmd) {
    if (cmd === 'help') {
      term.writeln(' 사용 가능한 명령어(로그인 필요)')
      term.writeln('    - like all')
      term.writeln('    - like all series = {name}')
      term.writeln('    - series change {old} to {new}')
      term.write(prompt())
      return
    }

    fetch('/api/execute-command', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ command: cmd }),
    })
      .then(res => res.json())
      .then(data => {
        term.writeln('\r\n' + data.result)
        term.write(prompt())
      })
      .catch(err => {
        term.writeln('\r\nError: ' + err)
        term.write(prompt())
      })
  }

  function initTerminal () {
    term = new Terminal({
      fontFamily: 'monospace',
      fontSize: 16,
      lineHeight: 1.2,
      cols: 100,
      rows: 22,
      cursorBlink: true,
      theme: {
        background: '#bdd4e7',
        foreground: '#000000',
        cursor: '#007bff',
        cursorAccent: '#ffffff',
      },
    })

    term.open(document.getElementById('terminal'))
    term.focus()

    term.writeln('')
    term.writeln(' 🚀 NOTEJ에 오신걸 환영합니다! ' + userColor + '\'help\'' + resetColor + ' 명령어를 입력해보세요.')
    term.writeln('')
    printAsciiArt(term) // ✅ 여기서 호출
    term.write(prompt())

    term.onData(data => {
      if (data === '\x1b[A') {
        if (commandHistory.length > 0 && historyIndex > 0) {
          historyIndex--
          commandBuffer = commandHistory[historyIndex]
          redrawCommandBuffer()
        }
      } else if (data === '\x1b[B') {
        if (commandHistory.length > 0) {
          if (historyIndex < commandHistory.length - 1) {
            historyIndex++
            commandBuffer = commandHistory[historyIndex]
          } else {
            historyIndex = commandHistory.length
            commandBuffer = ''
          }
          redrawCommandBuffer()
        }
      } else if (data === '\x7F' || data === '\b') {
        if (commandBuffer.length > 0) {
          commandBuffer = commandBuffer.slice(0, -1)
          redrawCommandBuffer()
        }
      } else if (data === '\r') {
        term.write('\r\n')
        if (commandBuffer.trim() !== '') {
          commandHistory.push(commandBuffer)
          if (commandHistory.length > 30) commandHistory.shift()
          historyIndex = commandHistory.length

          if (userStore.username !== 'nologin') {
            processCommand(commandBuffer)
          } else {
            term.writeln('⚠️  로그인이 필요합니다 🚫')
            term.write(prompt())
          }
        } else {
          term.write(prompt())
        }
        commandBuffer = ''
      } else {
        commandBuffer += data
        term.write(userColor + data + resetColor)
      }
    })
  }

  onMounted(() => {
    initTerminal()
  })
</script>

<style scoped>
.terminal-container {
  width: 100%;
  height: 500px;
  border: 1px solid #ccc;
  border-radius: 6px;
  overflow: hidden;
}
</style>
