(function(global) {
  const defaultTheme = '/img/chesspieces/wikipedia/{piece}.svg';

  function fenToBoard(fen) {
    const rows = fen.split(' ')[0].split('/');
    const board = [];
    for (let r = 0; r < 8; r++) {
      const row = [];
      let idx = 0;
      for (const ch of rows[r]) {
        if (ch >= '1' && ch <= '8') {
          const empty = parseInt(ch, 10);
          for (let k = 0; k < empty; k++) row.push('');
        } else {
          row.push(ch);
        }
      }
      board.push(row);
    }
    return board;
  }

  function boardToFen(board) {
    const rows = [];
    for (let r = 0; r < 8; r++) {
      let row = '';
      let empty = 0;
      for (let c = 0; c < 8; c++) {
        const cell = board[r][c];
        if (!cell) {
          empty++;
        } else {
          if (empty > 0) { row += empty; empty = 0; }
          row += cell;
        }
      }
      if (empty > 0) row += empty;
      rows.push(row);
    }
    return rows.join('/');
  }

  function squareName(row, col, orientation) {
    const filesWhite = ['a','b','c','d','e','f','g','h'];
    const filesBlack = ['h','g','f','e','d','c','b','a'];
    const files = orientation === 'black' ? filesBlack : filesWhite;
    const file = files[col];
    const rank = orientation === 'black' ? row + 1 : 8 - row;
    return file + rank;
  }

  function squareToCoords(square, orientation) {
    if (!square || square.length !== 2) return null;
    const fileChar = square[0];
    const rank = parseInt(square[1], 10);
    if (isNaN(rank) || rank < 1 || rank > 8) return null;
    const filesWhite = ['a','b','c','d','e','f','g','h'];
    const filesBlack = ['h','g','f','e','d','c','b','a'];
    const files = orientation === 'black' ? filesBlack : filesWhite;
    const col = files.indexOf(fileChar);
    if (col === -1) return null;
    const row = orientation === 'black' ? rank - 1 : 8 - rank;
    return { row, col };
  }

  function render(boardEl, board, orientation, theme, onDrop) {
    boardEl.innerHTML = '';
    boardEl.className = 'chessboard-container';
    for (let r = 0; r < 8; r++) {
      for (let c = 0; c < 8; c++) {
        const isLight = (r + c) % 2 === 0;
        const square = document.createElement('div');
        square.className = 'chessboard-square ' + (isLight ? 'light' : 'dark');
        const squareNameStr = squareName(r, c, orientation);
        square.dataset.square = squareNameStr;
        square.addEventListener('dragover', ev => ev.preventDefault());
        square.addEventListener('drop', ev => {
          ev.preventDefault();
          const source = ev.dataTransfer.getData('source-square');
          if (source) {
            const result = onDrop ? onDrop(source, squareNameStr) : undefined;
            if (result === 'snapback') {
              // no-op; caller handles position reset
            }
          }
        });
        const piece = board[r][c];
        if (piece) {
          const img = document.createElement('img');
          img.className = 'chessboard-piece';
          const pieceKey = piece === piece.toUpperCase() ? 'w' + piece.toUpperCase() : 'b' + piece.toUpperCase();
          const src = theme.replace('{piece}', pieceKey);
          img.setAttribute('draggable', 'true');
          img.addEventListener('dragstart', ev => {
            ev.dataTransfer.effectAllowed = 'move';
            ev.dataTransfer.setData('source-square', squareNameStr);
          });
          img.src = src;
          square.appendChild(img);
        }
        boardEl.appendChild(square);
      }
    }
  }

  function Chessboard(elementIdOrEl, config) {
    const cfg = Object.assign({
      position: 'start',
      orientation: 'white',
      draggable: true,
      pieceTheme: defaultTheme,
      onDrop: null
    }, config || {});

    const el = typeof elementIdOrEl === 'string' ? document.getElementById(elementIdOrEl) : elementIdOrEl;
    if (!el) throw new Error('Chessboard: element not found');

    let orientation = cfg.orientation === 'black' ? 'black' : 'white';
    let boardState = cfg.position === 'start'
      ? fenToBoard('rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR')
      : fenToBoard(cfg.position);

    function redraw() {
      render(el, boardState, orientation, cfg.pieceTheme || defaultTheme, cfg.onDrop);
    }

    redraw();

    return {
      position: function(fenOrObj, animate) {
        if (typeof fenOrObj === 'string') {
          boardState = fenToBoard(fenOrObj);
        }
        redraw();
      },
      orientation: function(ori) {
        if (!ori) return orientation;
        orientation = ori === 'black' ? 'black' : 'white';
        redraw();
      },
      fen: function() {
        return boardToFen(boardState);
      }
    };
  }

  if (typeof module !== 'undefined' && module.exports) {
    module.exports = Chessboard;
  } else {
    global.Chessboard = Chessboard;
  }
})(this);
