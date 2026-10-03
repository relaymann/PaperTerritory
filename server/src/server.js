import http from"node:http";import{WebSocketServer}from"ws";import crypto from"node:crypto";
const W=180,H=120,CELL=4,TICK=50,MAX=8,colors=[[65,150,255],[255,78,96],[83,220,125],[190,95,255],[255,180,55],[40,210,210],[255,105,180],[170,210,70]],players=new Map(),rooms=new Map();let publicRoom=null;
const key=(x,y)=>x+","+y,clamp=(v,a,b)=>Math.max(a,Math.min(b,v)),spawn=i=>({x:25+(i%4)*40,y:20+Math.floor(i/4)*35});
function home(p){const r=6,cx=Math.floor(p.x/CELL),cy=Math.floor(p.y/CELL);for(let y=-r;y<=r;y++)for(let x=-r;x<=r;x++){const a=cx+x,b=cy+y;if(a>=0&&b>=0&&a<W/CELL&&b<H/CELL)p.cells.add(key(a,b))}}
function inside(p){return p.cells.has(key(Math.floor(p.x/CELL),Math.floor(p.y/CELL)))}
function fill(p){
 if(p.trail.length<2)return;
 const gw=W/CELL,gh=H/CELL,blocked=new Set(p.cells);
 for(const q of p.trail){const cx=Math.floor(q[0]/CELL),cy=Math.floor(q[1]/CELL);if(cx>=0&&cy>=0&&cx<gw&&cy<gh)blocked.add(key(cx,cy))}
 const outside=new Set(),queue=[];
 for(let x=0;x<gw;x++){queue.push([x,0],[x,gh-1])}
 for(let y=0;y<gh;y++){queue.push([0,y],[gw-1,y])}
 while(queue.length){const [x,y]=queue.pop(),k=key(x,y);if(x<0||y<0||x>=gw||y>=gh||outside.has(k)||blocked.has(k))continue;outside.add(k);queue.push([x+1,y],[x-1,y],[x,y+1],[x,y-1])}
 for(let y=0;y<gh;y++)for(let x=0;x<gw;x++){const k=key(x,y);if(!outside.has(k))p.cells.add(k)}
 for(const o of players.values())if(o!==p&&o.room===p.room)for(const c of [...o.cells])if(p.cells.has(c))o.cells.delete(c);
 p.trail=[]
}
function die(p){p.alive=false;p.trail=[];setTimeout(()=>{if(p.ws.readyState!==1)return;p.alive=true;let q=spawn(Math.floor(Math.random()*8));p.x=q.x;p.y=q.y;p.cells=new Set();home(p)},1200)}
function join(p,code,priv){let r=rooms.get(code);if(!r){r={id:code,private:priv,members:new Set()};rooms.set(code,r)}r.members.add(p.id);p.room=code}
function leave(p){if(!p.room)return;const r=rooms.get(p.room);r?.members.delete(p.id);if(r?.members.size===0){rooms.delete(p.room);if(publicRoom===p.room)publicRoom=null}}
function state(r,self){const a=[...r.members].map(id=>players.get(id)).filter(Boolean),cells={};for(const p of a)for(const c of p.cells)cells[c]=p.id;return JSON.stringify({type:"state",width:W,height:H,selfId:self.id,room:r.id,players:a.map(p=>({id:p.id,name:p.name,x:p.x,y:p.y,r:p.color[0],g:p.color[1],b:p.color[2],area:p.cells.size,alive:p.alive,trail:p.trail.slice(-160)})),cells})}
function broadcast(){for(const r of rooms.values()){const a=[...r.members].map(id=>players.get(id)).filter(Boolean);for(const p of a)if(p.ws.readyState===1)p.ws.send(state(r,p))}}
function player(ws){const i=players.size,q=spawn(i),p={id:crypto.randomUUID(),name:"Player",ws,x:q.x,y:q.y,dx:1,dy:0,color:colors[i%colors.length],cells:new Set(),trail:[],alive:true,room:null};home(p);players.set(p.id,p);return p}
const server=http.createServer((req,res)=>{res.writeHead(200,{"content-type":"application/json"});res.end(JSON.stringify({ok:true,players:players.size,rooms:rooms.size}))}),wss=new WebSocketServer({server});
wss.on("connection",ws=>{const p=player(ws);ws.on("message",raw=>{try{const m=JSON.parse(raw);if(m.type==="join"){p.name=String(m.name||"Player").slice(0,16);if(m.private){const code=String(m.room||"").toUpperCase();if(!/^[A-Z0-9]{5}$/.test(code))return;if((rooms.get(code)?.members.size||0)>=MAX)return;join(p,code,true)}else{if(!publicRoom||!rooms.get(publicRoom)||rooms.get(publicRoom).members.size>=MAX)publicRoom="PUBLIC-"+crypto.randomBytes(3).toString("hex");join(p,publicRoom,false)}}if(m.type==="move"){const x=Number(m.dx),y=Number(m.dy),l=Math.hypot(x,y);if(Number.isFinite(l)&&l>.01){p.dx=x/l;p.dy=y/l}}}catch{}});ws.on("close",()=>{leave(p);players.delete(p.id);broadcast()})});
setInterval(()=>{for(const p of players.values()){if(!p.room||!p.alive)continue;const before=inside(p);p.x=clamp(p.x+p.dx*.95,1,W-1);p.y=clamp(p.y+p.dy*.95,1,H-1);const after=inside(p);if(before&&!after)p.trail=[[p.x,p.y]];else if(!after&&p.trail.length){const q=p.trail[p.trail.length-1];if(Math.hypot(q[0]-p.x,q[1]-p.y)>1.5)p.trail.push([p.x,p.y])}else if(!before&&after)fill(p);if(!after&&p.trail.length>320){die(p);continue}for(const o of players.values())if(o!==p&&o.room===p.room&&o.alive)for(const q of o.trail)if(Math.hypot(q[0]-p.x,q[1]-p.y)<2.8){die(o);break}}broadcast()},TICK);
const port=process.env.PORT||3000;server.listen(port,"0.0.0.0",()=>console.log("PaperTerritory server listening on "+port));