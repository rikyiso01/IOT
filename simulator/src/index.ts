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
    private subscribeTopic:string|null
  ) {
  }
  public async send(obj: RequestType): Promise<ResponseType> {
      const client=await connectAsync("mqtt://212.78.1.205:1883", { username: "studenti",password:"studentiDRUIDLAB_1" });
      await client.publishAsync(this.publishTopic,JSON.stringify(obj))
      if(this.subscribeTopic==null)return null as any;
      await client.subscribeAsync(this.subscribeTopic)
      const promise=new Promise<ResponseType>((resolve)=>{client.on('message',(_,message)=>{console.log(message.toString());resolve(JSON.parse(message.toString()))})});
      const result=await promise;
      await client.endAsync();
      return result
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

async function sleep(s:number) {
  await new Promise((resolve) => {
    setTimeout(resolve, s*1000);
  });
}

function randomNumber(min:number,max:number){
    return Math.random()*(max-min)+min;
}


async function simulator(n:number|string) {
    n=Number(n.toString())
    for(let i=0;i<n;i++){
        singleInstance(`simulation${i}`,`simulation${i}`,i);
    }
}

async function singleInstance(username:string,password:string,id:string|number){
    id=Number(id.toString())
    await register(username,password);
    const obj=await login(username,password);
    const token=obj.token;
    if(token===null)throw new Error("Login failed");
    await pair(id,token);
    while(true){
        await sendData(id,randomNumber(0,1));
        console.log("Sent, looping");
        await sleep(1);
    }
}

async function register(username:string,password:string) {
    return (await REGISTER_API.send({username,password})).status;
}

async function login(username:string,password:string) {
    return await LOGIN_API.send({username,password});
}

async function pair(id: string|number, token: string) {
    id=Number(id.toString())
    return await PAIR_API.send({id:Number(id),token});
}

async function sendData(id:string|number,data:string|Number){
    return await new MQTTAPI<number,null>(`/Thingworx/Jug${id}/litresPerSecond`,null).send(Number(data));
}

function print(f:(..._:string[])=>Promise<any>){
    async function inner(...args:string[]){
        console.log(await f(...args));
    }
    return inner
}


program.command("register <username> <password>").action(print(register));
program.command("login <username> <password>").action(print(login));
program.command("pair <id> <token>").action(print(pair));
program.command("send-data <id> <data>").action(print(sendData));
program.command("simulator-single <username> <password> <id>").action(print(singleInstance));
program.command("simulator <n>").action(print(simulator));
program.parse();
