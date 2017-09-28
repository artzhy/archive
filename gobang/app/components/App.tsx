import * as React from 'react'
import Board from './Board'
import Controller from './Controller'
import Coordinates from './Coordinates'

export default class App extends React.Component<{},{}> {
  render() {
    return (
      <div style={{ display: 'flex' }}>
        <Coordinates />
        <Board />
        <Controller />
      </div>
    )
  }
}
