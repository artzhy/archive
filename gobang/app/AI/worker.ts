import { MessageType } from '../constants'
import AI from './AI'

const log = (...args: Array<any>) => console.log('[worker]:', ...args)

log('started')

onmessage = (event: MessageEvent) => {
  const message = JSON.parse(event.data)
  log('received', message)
  if (message.type === MessageType.ANALYSE) {
    const { pieces, aiSide } = message
    const t = AI(pieces, aiSide)

    postMessage(JSON.stringify({ type: MessageType.STEP, t, side: aiSide }), undefined)
  }
}
