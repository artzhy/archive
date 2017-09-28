class DebugInfo {
  total: number
  hit: number
  real: number

  constructor() {
    this.total = 0
    this.hit = 0
    this.real = 0
  }

  reset() {
    this.total = 0
    this.hit = 0
    this.real = 0
  }
}

export default new DebugInfo()
