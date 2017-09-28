import { Map, List, Repeat } from 'immutable'
import { Side, Stage, SIZE, ActionType } from './constants'
import { swapSide, isGameover } from './common'

export interface State {
  stage: Stage
  pieces: List<Side>
  lastT?: number
  side?: Side
  playerSide?: Side
}

export type StateType = Map<string, any>

const initialState: StateType = Map({
  stage: Stage.INIT,
  pieces: Repeat(Side.none, SIZE * SIZE).toList(),
  lastT: -1, // 最后一步的位置
  side: null,
  playerSide: Side.black, // 玩家总是黑棋
})

export const reducer: Redux.Reducer<StateType> = function (state = initialState, action: any) {
  if (action.type === ActionType.STEP) {
    const pieces = state.get('pieces').set(action.t, state.get('side'))
    const gameover = isGameover(pieces, action.t)
    if (gameover) {
      console.warn('GAME OVER') // todo
    }
    return state.set('pieces', pieces)
      .update('side', swapSide)
      .set('lastT', action.t)
      .set('stage', gameover ? Stage.GAMEOVER : Stage.ON)
  } else if (action.type === ActionType.RESTART) {
    return state.set('stage', Stage.ON)
      .set('side', Side.black)
      .set('pieces', Repeat(Side.none, SIZE * SIZE).toList())
      .set('lastT', -1)
  } else {
    return state
  }
}
