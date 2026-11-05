// Run with FOODNOW_ADMIN_PASSWORD set in the process environment; never save tokens.
const fs=require('fs');
const base='https://foodnow-app-22dff-default-rtdb.asia-southeast1.firebasedatabase.app';
async function main(){
 const key=JSON.parse(fs.readFileSync('app/google-services.json')).client[0].api_key[0].current_key;
 const response=await fetch('https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key='+key,{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({email:'admin@gmail.com',password:process.env.FOODNOW_ADMIN_PASSWORD,returnSecureToken:true})});
 const auth=await response.json(); if(!response.ok) throw Error(auth.error.message);
 async function request(path,method='GET',body){const r=await fetch(base+'/'+path+'.json?auth='+auth.idToken,{method,headers:{'Content-Type':'application/json'},body:body===undefined?undefined:JSON.stringify(body)});const data=await r.json();if(!r.ok)throw Error(path+': '+JSON.stringify(data));return data;}
 const existing={};for(const name of ['users','restaurants','foods','options','orders'])existing[name]=await request(name)||{};
 if(existing.users[auth.localId]?.role!=='admin') throw Error('Admin profile missing');
 if(process.argv.includes('--inspect')){console.log(JSON.stringify({users:existing.users,counts:Object.fromEntries(Object.entries(existing).map(([k,v])=>[k,Object.keys(v).length]))},null,2));return;}
 fs.mkdirSync('firebase-backups',{recursive:true});
 const backup='firebase-backups/before-demo-'+Date.now()+'.json';fs.writeFileSync(backup,JSON.stringify(existing,null,2));
 const catalog=JSON.parse(fs.readFileSync('app/src/main/assets/demo-catalog.json','utf8'));
 const updates={}, mapping={};
 for(const [id,r] of Object.entries(catalog.restaurants)){
  const match=Object.entries(existing.restaurants).find(([k,v])=>v.name.toLowerCase().includes(r.name.toLowerCase().split(' ')[0]));
  mapping[id]=match?match[0]:id;
  if(match) updates['restaurants/'+match[0]+'/logo']=r.logo;
  else updates['restaurants/'+id]=r;
 }
 for(const [id,f] of Object.entries(catalog.foods)){if(!existing.foods[id])updates['foods/'+id]={...f,restaurantKey:mapping[f.restaurantKey]};}
 for(const [id,o] of Object.entries(catalog.options))if(!existing.options[id])updates['options/'+id]=o;
 const customer=Object.entries(existing.users).find(([id,u])=>u.role==='user'&&!u.disabled);
 const shipper=Object.entries(existing.users).find(([id,u])=>u.role==='shipper'&&!u.disabled);
 if(!customer||!shipper)throw Error('Need existing user and shipper profiles before demo orders');
 const statuses=['Chờ xác nhận','Chờ xác nhận','Đang giao','Đã hoàn thành','Đã hoàn thành','Đã hủy'];
 statuses.forEach((status,i)=>{
  const id='demo-order-v1-'+(i+1);if(existing.orders[id])return;
  const foodKey='demo-f'+(i+7),food=existing.foods[foodKey]||catalog.foods[foodKey];
  const assigned=status==='Đang giao'||status==='Đã hoàn thành';
  updates['orders/'+id]={ownerUid:customer[0],userEmail:customer[1].email,totalPrice:food.price*(i%2+1),date:'2026-11-05 '+String(10+i).padStart(2,'0')+':00',status,shipperUid:assigned?shipper[0]:'',shipperName:assigned?shipper[1].fullName:'',paymentStatus:'simulated_paid',deliveryProgress:status==='Đã hoàn thành'?100:status==='Đang giao'?40:0,itemsJson:JSON.stringify([{foodKey,foodName:food.name+' [ĐƠN MẪU]',price:food.price,quantity:i%2+1}])};
 });
 // Atomic multi-location patch preserves all other existing data.
 await request('','PATCH',updates);
 for(const path of Object.keys(updates)){const saved=await request(path);if(!require('util').isDeepStrictEqual(saved,updates[path]))throw Error('Verification failed: '+path);}
 const counts={};for(const name of ['restaurants','foods','options','orders']) counts[name]=Object.keys(await request(name)||{}).length;
 console.log(JSON.stringify({backup,writtenPaths:Object.keys(updates).length,counts,customer:customer[1].email,shipper:shipper[1].email},null,2));
}
main().catch(e=>{console.error(e.message);process.exitCode=1});
