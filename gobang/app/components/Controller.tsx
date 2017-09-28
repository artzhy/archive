import * as React from 'react'
import { connect } from 'react-redux'
import { StateType } from '../reducer'
import { ActionType } from '../constants'

interface ControllerProps {
  stage: string
  side: string
  dispatch: Redux.Dispatch<StateType>,
}

class Controller extends React.Component<ControllerProps, {}> {
  render() {
    // const { side, stage } = this.props
    return (
      <div>
        <h1 style={{ color: 'red' }}>这里是游戏状态</h1>
        <button onClick={() => this.props.dispatch({ type: ActionType.RESTART })}>开始游戏</button>
      </div>
    )
  }
}

export default connect((state: any) => state.toObject())(Controller)
