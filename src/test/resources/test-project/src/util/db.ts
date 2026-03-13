// Database wrapper
// TODO: add connection pooling
export class Database {
  private name: string
  private connected: boolean = false

  constructor(name: string) {
    this.name = name
  }

  async connect() {
    // TODO: add retry logic
    this.connected = true
    console.log(`Connected to ${this.name}`)
  }

  async query(sql: string, params?: any[]) {
    if (!this.connected) {
      throw new Error("Not connected")
    }
    // TODO: add query logging
    return []
  }

  async close() {
    this.connected = false
  }
}
