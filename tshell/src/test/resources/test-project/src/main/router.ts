export class Router {
  private routes: Map<string, Function> = new Map()

  get(path: string, handler: Function) {
    this.routes.set(`GET:${path}`, handler)
  }

  post(path: string, handler: Function) {
    this.routes.set(`POST:${path}`, handler)
  }

  listen(port: number) {
    console.log(`Router listening on ${port}`)
  }
}
