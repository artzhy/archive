import { select, fork } from 'redux-saga/effects'
import { takeEvery } from 'redux-saga'
import { Side, Stage, ActionType } from '../constants'
import { State } from '../reducer'
import workerSaga from './workerSaga'

export function* watchStep(action: any): any {
  const { stage, pieces, side } : State = (yield select()).toObject()
  if (stage === Stage.ON && side === Side.white) {
    // 该让电脑电脑下棋了
  }
}

export default function* rootSaga(): any {
  yield takeEvery(ActionType.STEP, watchStep)
  yield fork(workerSaga)
}
