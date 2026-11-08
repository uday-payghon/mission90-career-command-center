// Extend existing screens without replacing the dashboard or preview mode.
const baseRender=render;
render=function(){
  baseRender();
  document.querySelectorAll('.focus-item span').forEach(label=>{if(label.textContent==='Focused time completed')label.textContent='Planned time of completed tasks';});
  if(state.view==='today'&&state.mode==='demo')document.querySelector('.view-head p').textContent='Browser preview only. Changes are not stored in PostgreSQL.';
  if(!document.getElementById('signout')&&state.mode==='server'){
    const button=document.createElement('button');button.id='signout';button.className='linkbtn';button.textContent='SIGN OUT';
    button.addEventListener('click',async()=>{button.disabled=true;try{const response=await fetch('/logout',{method:'POST',credentials:'same-origin',headers:{'X-XSRF-TOKEN':state.csrf}});if(!response.ok)throw Error('Sign-out failed. Reload and retry.');location.href='/login.html?logout';}catch(error){notify(error.message);button.disabled=false;}});
    document.querySelector('.topright').append(button);
  }
  document.querySelectorAll('label:not([for])').forEach((label,index)=>{
    const input=label.parentElement.querySelector('input,select,textarea');
    if(input){input.id||='field-'+index;label.htmlFor=input.id;}
  });
  if(state.view==='today')document.querySelectorAll('[data-task]').forEach(checkbox=>{
    const item=state.data.tasks.find(t=>String(t.id)===checkbox.dataset.task);
    const form=document.createElement('form');form.className='task-detail';form.dataset.taskEdit=item.id;
    form.innerHTML=`<details><summary>Notes, priority & work evidence</summary><div class="entryform">
      <div class="field"><label>Priority<select name="priority">${['Low','Normal','High'].map(p=>`<option ${item.priority===p?'selected':''}>${p}</option>`).join('')}</select></label></div>
      <div class="field"><label>Actual minutes<input name="actualMinutes" type="number" min="0" max="840" value="${item.actualMinutes||0}" required></label></div>
      <div class="field wide"><label>Notes / challenge<textarea name="note" maxlength="1200">${esc(item.note||'')}</textarea></label></div>
      <div class="field wide"><label>Commit / deliverable URL<input name="deliverableLink" type="url" maxlength="1200" value="${esc(item.deliverableLink||'')}" placeholder="https://github.com/…"></label></div>
      <button type="submit" class="primary">Save task details</button></div></details>`;
    checkbox.closest('.task').querySelector('.tasktext').append(form);
  });
  const dsaForm=document.getElementById('dsaform');
  if(dsaForm){
    extraDate(dsaForm,'revisionOn','Revision date');
    document.querySelectorAll('[data-del-dsa]').forEach(button=>{
      const row=state.dsa.find(x=>String(x.id)===button.dataset.delDsa);
      const edit=document.createElement('button');edit.className='linkbtn';edit.textContent='EDIT';edit.type='button';
      edit.addEventListener('click',()=>fillEdit(dsaForm,row));button.before(edit);
      const details=document.createElement('p');details.className='muted';details.textContent=`${row.minutes||0} min · Revision: ${row.revisionOn||'Not set'} · ${row.insight||'No notes yet'}`;
      button.closest('tr').querySelector('td').append(details);
    });
  }
  const jobForm=document.getElementById('jobform');
  if(jobForm){
    extraDate(jobForm,'interviewOn','Interview date');extraDate(jobForm,'followUpOn','Follow-up date');
    document.querySelectorAll('[data-app-stage]').forEach(select=>{
      const row=state.applications.find(x=>String(x.id)===select.dataset.appStage);
      const edit=document.createElement('button');edit.className='linkbtn';edit.textContent='EDIT';edit.type='button';
      edit.addEventListener('click',()=>fillEdit(jobForm,row));select.closest('tr').querySelector('td').append(edit);
      const remove=document.createElement('button');remove.className='linkbtn';remove.textContent='DELETE';remove.type='button';remove.setAttribute('aria-label','Delete '+row.company);
      remove.addEventListener('click',async()=>{if(!confirm('Delete this application?'))return;try{if(state.mode==='server')await api('/applications/'+row.id,{method:'DELETE'});else{const data=getDemo();data.applications=data.applications.filter(x=>x.id!==row.id);saveDemo(data);}await refresh();}catch(error){notify(error.message);}});edit.after(remove);
      const dates=document.createElement('p');dates.className='muted';dates.textContent=`Interview: ${row.interviewOn||'Not set'} · Follow-up: ${row.followUpOn||'Not set'} · ${row.notes||''}`;
      select.closest('tr').querySelector('td').append(dates);
    });
  }
  if(state.view==='overview'&&state.mode==='server'){
    const stats=state.stats;
    if(stats?.week){
      const box=document.createElement('section');box.className='panel';
      box.innerHTML=`<h3>Weekly & monthly progress</h3><p>This week: ${stats.week.done}/${stats.week.total} · ${pct(stats.week.done,stats.week.total)}%. This month: ${stats.month.done}/${stats.month.total} · ${pct(stats.month.done,stats.month.total)}%.</p><h3>Upcoming interviews & follow-ups</h3>`;
      const upcoming=stats.upcoming||[];
      for(const row of upcoming){const line=document.createElement('p');line.textContent=`${row.company} · Interview: ${row.interviewOn||'Not set'} · Follow-up: ${row.followUpOn||'Not set'} · Deadline: ${row.deadline||'Not set'}`;box.append(line);}
      if(!upcoming.length){const line=document.createElement('p');line.textContent='No dates recorded. Add verified dates in your placement pipeline.';box.append(line);}
      document.getElementById('view').append(box);
    }
  }
};
function extraDate(form,name,title){const field=document.createElement('div');field.className='field';const label=document.createElement('label');label.textContent=title;const input=document.createElement('input');input.type='date';input.name=name;label.append(input);field.append(label);form.insertBefore(field,form.lastElementChild);}
function fillEdit(form,row){form.dataset.editId=row.id;for(const [key,value] of Object.entries(row)){const field=form.elements.namedItem(key);if(field)field.value=value??'';}form.querySelector('[type=submit]').textContent='Save changes';form.scrollIntoView({behavior:'smooth',block:'center'});}
document.addEventListener('submit',async event=>{
  const form=event.target;
  if(!form.dataset.taskEdit&&!form.dataset.editId)return;
  event.preventDefault();event.stopImmediatePropagation();
  const button=form.querySelector('[type=submit]');button.disabled=true;
  const body=formData(form);
  try{
    if(form.dataset.taskEdit){
      body.actualMinutes=Number(body.actualMinutes);
      if(state.mode==='server')await api('/tasks/'+form.dataset.taskEdit,{method:'PATCH',body:JSON.stringify(body)});
      else {const data=getDemo();data.tasks??={};data.tasks[form.dataset.taskEdit]={...data.tasks[form.dataset.taskEdit],...body};saveDemo(data);}
    }else if(form.id==='dsaform'){
      body.minutes=Number(body.minutes);
      if(state.mode==='server')await api('/dsa/'+form.dataset.editId,{method:'PUT',body:JSON.stringify(body)});
      else{const data=getDemo();Object.assign(data.dsa.find(x=>String(x.id)===form.dataset.editId),body);saveDemo(data);}
    }else if(form.id==='jobform'){
      if(state.mode==='server')await api('/applications/'+form.dataset.editId,{method:'PATCH',body:JSON.stringify(body)});
      else{const data=getDemo();Object.assign(data.applications.find(x=>String(x.id)===form.dataset.editId),body);saveDemo(data);}
    }
    await refresh();notify('Changes saved');
  }catch(error){notify(error.message);}finally{button.disabled=false;}
},true);
