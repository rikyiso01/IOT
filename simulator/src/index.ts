import { connectAsync } from "mqtt";
import { program } from "commander";
import fetch from "node-fetch";

const HOST = "127.0.0.1";
const PORT = 1881;

enum Method {
  GET = "get",
  POST = "post",
}

class RestAPI<RequestType, ResponseType> {
  constructor(
    private path: string,
    private method: Method,
  ) {}
  public async send(obj: RequestType): Promise<ResponseType> {
    const response = await fetch(`http://${HOST}:${PORT}/${this.path}`, {
      method: this.method,
      body: JSON.stringify(obj),
    });
    return (await response.json()) as ResponseType;
  }
}

interface RegisterRequest{
    username:string,
    password:string
}

enum RegisterResponseStatus{
    OK="OK"
}

interface RegisterResponse{
    status:RegisterResponseStatus
}

const REGISTER_API=new RestAPI<RegisterRequest,RegisterResponse>("register",Method.POST);

interface LoginRequest{
    username:string
    password:string
}

enum LoginResponseStatus{
    OK="OK"
}

interface LoginResponse{
    status:LoginResponseStatus
    token:string|null
}

const LOGIN_API=new RestAPI<LoginRequest,LoginResponse>("login",Method.POST);


class MQTTAPI<RequestType,ResponseType>{
  constructor(
    private publishTopic: string,
    private subscribeTopic:string
  ) {
  }
  public async send(obj: RequestType): Promise<ResponseType> {
      const client=await connectAsync("mqtt://127.0.0.1:1883", { username: "studenti",password:"studentiDRUIDLAB_1" });
      await client.publishAsync(this.publishTopic,JSON.stringify(obj))
      await client.subscribeAsync(this.subscribeTopic)
      const promise=new Promise<ResponseType>((resolve)=>{client.on('message',(message)=>{resolve(JSON.parse(message))})});
      return await promise;
  }
}

interface PairRequest{
    id:number,
    token:string,
}

enum PairResponseStatus{
    OK="OK"
}

interface PairResponse{
    status:PairResponseStatus
}

const PAIR_API=new MQTTAPI<PairRequest,PairResponse>("/jug/pair","/jug/pair/response");


async function connect(host: string, port: string) {
  await connectAsync("mqtt://127.0.0.1:1883", { username: "" });
}

async function register(username:string,password:string) {
    console.log((await REGISTER_API.send({username,password})).status)
}

async function login(username:string,password:string) {
    console.log(await LOGIN_API.send({username,password}));
}

async function pair(id: string, token: string) {
    console.log(await PAIR_API.send({id:Number(id),token}));
}

program.command("connect <host> <ip>").action(connect);
program.command("register <username> <password>").action(register);
program.command("login <username> <password>").action(login);
program.command("pair <id> <token>").action(pair);
program.parse();
