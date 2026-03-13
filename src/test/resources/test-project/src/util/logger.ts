export type LogLevel = "debug" | "info" | "warn" | "error"

export class Logger {
  private level: LogLevel
  private prefix: string

  constructor(prefix: string, level: LogLevel = "info") {
    this.prefix = prefix
    this.level = level
  }

  debug(msg: string) { this.log("debug", msg) }
  info(msg: string) { this.log("info", msg) }
  warn(msg: string) { this.log("warn", msg) }
  error(msg: string) { this.log("error", msg) }

  private log(level: LogLevel, msg: string) {
    const timestamp = new Date().toISOString()
    console.log(`[${timestamp}] [${level.toUpperCase()}] ${this.prefix}: ${msg}`)
  }
}
