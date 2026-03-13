import { Router } from "./router"
import { Database } from "../util/db"

// TODO: add authentication middleware
export class App {
  private router: Router
  private db: Database

  constructor() {
    this.router = new Router()
    this.db = new Database("main")
  }

  start(port: number) {
    // TODO: add graceful shutdown
    console.log(`Starting on port ${port}`)
    this.router.listen(port)
  }

  async getUsers() {
    return this.db.query("SELECT * FROM users")
  }

  async getUser(id: number) {
    // TODO: add input validation
    return this.db.query("SELECT * FROM users WHERE id = ?", [id])
  }
}
