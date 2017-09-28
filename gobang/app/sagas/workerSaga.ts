import { take, select, fork, put } from 'redux-saga/effects'
import { eventChannel, buffers } from 'redux-saga'
import { State } from '../reducer'
import { Stage, MessageType, ActionType } from '../constants'
import { swapSide } from '../common'
const Worker = require('worker-loader!../AI/worker')

const worker = new Worker()

const channel = eventChannel((emmiter) => {
  const listener = (event: MessageEvent) => {
    emmiter(JSON.parse(event.data))
  }
  worker.addEventListener('message', listener)
  return () => worker.removeEventListener('message', listener)
}, buffers.expanding(16))

function* handleWorkerMessage(): IterableIterator<any> {
  while (true) {
    const message = yield take(channel)
    if (message.type === MessageType.STEP) {
      yield put(message) // when type is STEP, message is just an action
    }
  }
}

export default function* workerSaga(): IterableIterator<any> {
  yield fork(handleWorkerMessage)

  while (true) {
    yield take(ActionType.STEP)
    const { pieces, side, playerSide, stage } : State = (yield select()).toObject()
    if (stage === Stage.ON && side !== playerSide) {
      const aiSide = swapSide(playerSide)
      worker.postMessage(JSON.stringify({ type: MessageType.ANALYSE, aiSide, pieces }))
    }
  }
}
