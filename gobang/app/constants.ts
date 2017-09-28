export const SIZE = 15
export const SIZE_SQUARE = 225
export const GRID_SIZE = 40
export const IMMEDIATE_WIN = 1 << 12
export const GO_BANG = 1 << 16

// todo 棋子大小 0.45或是0.48 需要设置为一个常量

export const BOARD_DX = GRID_SIZE
export const BOARD_DY = GRID_SIZE

export enum Side {
  black = 0,
  white = 1,
  none,
}

export const MessageType = {
  ANALYSE: 'ANALYSE',
  STEP: 'STEP',
}

export const ActionType = {
  RESTART: 'RESTART',
  STEP: 'STEP',
}

/** 游戏的不同阶段 */
export enum Stage {
  INIT,
  ON,
  GAMEOVER,
}
