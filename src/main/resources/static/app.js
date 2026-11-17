/**
 * 9STRIDE//OPS — Carter's Training Logbook
 * Application Frontend Controller & REST API Integration
 *
 * NOTE: All modal triggers, form submissions, and auto-calculator
 * handlers are attached directly to window.* for robust inline HTML onclick support.
 */

let cachedRuns = [];

const WEEK_DATES = [
  '2026-08-24',
  '2026-08-25',
  '2026-08-26',
  '2026-08-27',
  '2026-08-28',
  '2026-08-29',
  '2026-08-30'
];

// ==========================================
// 1. GLOBAL WINDOW-ATTACHED MODAL HANDLERS
// ==========================================

/**
 * Open '+ Add Run' Modal
 * Can be called as:
 *   openAddRunModal() -> defaults to 2026-08-24, Run 1
 *   openAddRunModal('2026-08-24', 'Run 1') -> pre-fills date and slot
 *   openAddRunModal(element) -> extracts data-date and data-run-type
 */
window.openAddRunModal = function(arg1, arg2) {
  let defaultDate = '2026-08-24';
  let defaultSlot = 'Run 1';

  if (typeof arg1 === 'string' && arg1) {
    defaultDate = arg1;
    if (typeof arg2 === 'string' && arg2) {
      defaultSlot = arg2;
    }
  } else if (arg1 && typeof arg1 === 'object' && arg1.getAttribute) {
    defaultDate = arg1.getAttribute('data-date') || defaultDate;
    defaultSlot = arg1.getAttribute('data-run-type') || arg1.getAttribute('data-slot') || defaultSlot;
  }

  const form = document.getElementById('run-modal-form');
  if (form) form.reset();

  const idEl = document.getElementById('modal-run-id');
  if (idEl) idEl.value = '';

  const titleEl = document.getElementById('modal-title');
  if (titleEl) titleEl.textContent = 'Log Training Run';

  const isoDate = normalizeDateToIso(defaultDate);
  const dateEl = document.getElementById('modal-date');
  if (dateEl) dateEl.value = isoDate;

  const slotEl = document.getElementById('modal-run-type');
  if (slotEl) slotEl.value = defaultSlot || 'Run 1';

  const typeEl = document.getElementById('modal-workout-type');
  if (typeEl) typeEl.value = 'Normal Run';

  const milesEl = document.getElementById('modal-miles');
  if (milesEl) milesEl.value = '';

  const timeEl = document.getElementById('modal-time');
  if (timeEl) timeEl.value = '';

  const paceEl = document.getElementById('modal-pace');
  if (paceEl) paceEl.value = '';

  const notesEl = document.getElementById('modal-notes');
  if (notesEl) notesEl.value = '';

  const calcStatus = document.getElementById('modal-calc-status');
  if (calcStatus) calcStatus.textContent = 'Fill any 2 to auto-solve 3rd';

  const badgeEl = document.getElementById('modal-run-badge');
  if (badgeEl) badgeEl.textContent = `${formatIsoToShort(isoDate)} — ${defaultSlot}`;

  const deleteBtn = document.getElementById('modal-delete-btn');
  if (deleteBtn) deleteBtn.classList.add('hidden');

  showModalDialog();
};

/**
 * Close Add/Edit Run Modal
 */
window.closeAddRunModal = function() {
  const modalWrapper = document.getElementById('run-edit-modal-wrapper');
  const modalDialog = document.getElementById('run-edit-dialog');

  if (modalWrapper) {
    modalWrapper.classList.add('opacity-0', 'pointer-events-none');
  }
  if (modalDialog) {
    modalDialog.classList.remove('scale-100');
    modalDialog.classList.add('scale-95');
  }
};
window.closeRunEditModal = window.closeAddRunModal;

/**
 * Open Modal in Edit Mode for an existing Run ID
 */
window.openEditRunModal = function(runId) {
  const run = cachedRuns.find(r => String(r.id) === String(runId));
  if (!run) return;

  const idEl = document.getElementById('modal-run-id');
  if (idEl) idEl.value = run.id;

  const titleEl = document.getElementById('modal-title');
  if (titleEl) titleEl.textContent = 'Edit Training Run';

  const dateEl = document.getElementById('modal-date');
  if (dateEl) dateEl.value = run.runDate || '2026-08-24';

  const slotEl = document.getElementById('modal-run-type');
  if (slotEl) slotEl.value = run.sessionSlot || 'Run 1';

  const typeEl = document.getElementById('modal-workout-type');
  if (typeEl) typeEl.value = run.workoutType || 'Normal Run';

  const milesEl = document.getElementById('modal-miles');
  if (milesEl) milesEl.value = run.mileage != null ? run.mileage : '';

  const timeEl = document.getElementById('modal-time');
  if (timeEl) timeEl.value = run.totalTime || '';

  const paceEl = document.getElementById('modal-pace');
  if (paceEl) paceEl.value = run.pace || '';

  const notesEl = document.getElementById('modal-notes');
  if (notesEl) notesEl.value = run.notes || '';

  const calcStatus = document.getElementById('modal-calc-status');
  if (calcStatus) calcStatus.textContent = 'Fill any 2 to auto-solve 3rd';

  const badgeEl = document.getElementById('modal-run-badge');
  if (badgeEl) badgeEl.textContent = `${formatIsoToShort(run.runDate)} — ${run.sessionSlot || 'Run 1'}`;

  const deleteBtn = document.getElementById('modal-delete-btn');
  if (deleteBtn) deleteBtn.classList.remove('hidden');

  showModalDialog();
};

/**
 * Legacy compatibility: accepts element or id
 */
window.openRunEditModal = function(arg) {
  if (typeof arg === 'number' || (typeof arg === 'string' && !isNaN(arg))) {
    window.openEditRunModal(arg);
  } else if (arg && typeof arg === 'object' && arg.getAttribute) {
    const date = arg.getAttribute('data-date') || '2026-08-24';
    const runType = arg.getAttribute('data-run-type') || 'Run 1';
    window.openAddRunModal(date, runType);
  } else {
    window.openAddRunModal();
  }
};

/**
 * 3-Way Auto-Calculator: Distance (mi), Duration (h:mm:ss), Avg Pace (/mi)
 * Whenever any 2 values are present, auto-solves the 3rd.
 */
window.onCalcInput = function(changedField) {
  const milesInput = document.getElementById('modal-miles');
  const timeInput = document.getElementById('modal-time');
  const paceInput = document.getElementById('modal-pace');
  const statusEl = document.getElementById('modal-calc-status');

  if (!milesInput || !timeInput || !paceInput) return;

  const milesVal = parseFloat(milesInput.value);
  const timeSecs = parseTimeToSeconds(timeInput.value);
  const paceSecs = parseTimeToSeconds(paceInput.value);

  const hasMiles = !isNaN(milesVal) && milesVal > 0;
  const hasTime = timeSecs !== null && timeSecs > 0;
  const hasPace = paceSecs !== null && paceSecs > 0;

  if (changedField === 'miles') {
    if (hasMiles && hasTime) {
      const calcPace = timeSecs / milesVal;
      paceInput.value = formatSecondsToPace(calcPace);
      if (statusEl) statusEl.textContent = `Auto-calculated Pace: ${paceInput.value}/mi`;
    } else if (hasMiles && hasPace) {
      const calcTime = milesVal * paceSecs;
      timeInput.value = formatSecondsToTime(calcTime);
      if (statusEl) statusEl.textContent = `Auto-calculated Duration: ${timeInput.value}`;
    }
  } else if (changedField === 'time') {
    if (hasTime && hasMiles) {
      const calcPace = timeSecs / milesVal;
      paceInput.value = formatSecondsToPace(calcPace);
      if (statusEl) statusEl.textContent = `Auto-calculated Pace: ${paceInput.value}/mi`;
    } else if (hasTime && hasPace) {
      const calcMiles = timeSecs / paceSecs;
      milesInput.value = calcMiles.toFixed(2);
      if (statusEl) statusEl.textContent = `Auto-calculated Distance: ${milesInput.value} mi`;
    }
  } else if (changedField === 'pace') {
    if (hasPace && hasMiles) {
      const calcTime = milesVal * paceSecs;
      timeInput.value = formatSecondsToTime(calcTime);
      if (statusEl) statusEl.textContent = `Auto-calculated Duration: ${timeInput.value}`;
    } else if (hasPace && hasTime) {
      const calcMiles = timeSecs / paceSecs;
      milesInput.value = calcMiles.toFixed(2);
      if (statusEl) statusEl.textContent = `Auto-calculated Distance: ${milesInput.value} mi`;
    }
  }
};

/**
 * Handle form submission: POST to /api/runs (or delete existing + POST for edits)
 */
window.saveRun = async function(event) {
  if (event) event.preventDefault();

  const idEl = document.getElementById('modal-run-id');
  const runId = idEl ? idEl.value : '';

  const dateInput = document.getElementById('modal-date');
  const slotInput = document.getElementById('modal-run-type');
  const workoutTypeInput = document.getElementById('modal-workout-type');
  const milesInput = document.getElementById('modal-miles');
  const timeInput = document.getElementById('modal-time');
  const paceInput = document.getElementById('modal-pace');
  const notesInput = document.getElementById('modal-notes');

  let miles = parseFloat(milesInput ? milesInput.value : '0') || 0.0;
  let timeStr = timeInput ? timeInput.value.trim() : '';
  let paceStr = paceInput ? paceInput.value.trim() : '';

  // Auto-fill missing 3rd value if 2 are filled upon submitting
  const timeSecs = parseTimeToSeconds(timeStr);
  const paceSecs = parseTimeToSeconds(paceStr);
  if (miles > 0 && timeSecs > 0 && !paceSecs) {
    paceStr = formatSecondsToPace(timeSecs / miles);
  } else if (miles > 0 && paceSecs > 0 && !timeSecs) {
    timeStr = formatSecondsToTime(miles * paceSecs);
  } else if (timeSecs > 0 && paceSecs > 0 && miles <= 0) {
    miles = parseFloat((timeSecs / paceSecs).toFixed(2));
  }

  const payload = {
    athleteName: 'Carter',
    runDate: normalizeDateToIso(dateInput ? dateInput.value : '2026-08-24'),
    sessionSlot: slotInput ? slotInput.value : 'Run 1',
    mileage: miles,
    pace: paceStr || '—',
    totalTime: timeStr || '—',
    workoutType: workoutTypeInput ? workoutTypeInput.value : 'Normal Run',
    notes: notesInput ? notesInput.value.trim() : ''
  };

  try {
    // If editing, delete previous record first
    if (runId) {
      await fetch(`/api/runs/${runId}`, { method: 'DELETE' });
    }

    const res = await fetch('/api/runs', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });

    if (!res.ok) {
      throw new Error(`Failed to save run: ${res.status}`);
    }

    window.closeAddRunModal();
    await fetchAndRenderRuns();
  } catch (err) {
    console.error('Save run failed:', err);
    alert('Failed to save run to backend database. Please check console.');
  }
};
window.handleRunFormSubmit = window.saveRun;

/**
 * Handle Run Deletion: DELETE /api/runs/{id}
 */
window.handleModalDelete = async function() {
  const idEl = document.getElementById('modal-run-id');
  const runId = idEl ? idEl.value : '';
  if (!runId) return;

  if (!confirm('Are you sure you want to delete this run?')) {
    return;
  }

  try {
    const res = await fetch(`/api/runs/${runId}`, { method: 'DELETE' });
    if (!res.ok && res.status !== 404) {
      throw new Error(`Delete failed: ${res.status}`);
    }

    window.closeAddRunModal();
    await fetchAndRenderRuns();
  } catch (err) {
    console.error('Delete run failed:', err);
    alert('Failed to delete run from backend database.');
  }
};

// ==========================================
// 2. INJURY TELEMETRY MODAL HANDLERS
// ==========================================

window.openInjuryModal = function(date, status, area, side, pain, action, notes) {
  const modalWrapper = document.getElementById('injury-modal-wrapper');
  if (!modalWrapper) return;

  const dateBadge = document.getElementById('injury-modal-date-badge');
  if (dateBadge) dateBadge.textContent = `${date || '8/24'} — Health Status`;

  const areaEl = document.getElementById('injury-problem-area');
  if (areaEl) areaEl.value = area || 'None';

  const sideEl = document.getElementById('injury-side-location');
  if (sideEl) sideEl.value = side || 'Bilateral';

  const painSlider = document.getElementById('injury-pain-slider');
  if (painSlider) {
    painSlider.value = pain != null ? pain : 0;
    window.updatePainScoreDisplay(painSlider.value);
  }

  const actionEl = document.getElementById('injury-action-status');
  if (actionEl) actionEl.value = action || 'Cleared';

  const notesEl = document.getElementById('injury-symptom-notes');
  if (notesEl) notesEl.value = notes || '';

  modalWrapper.classList.remove('hidden');
};

window.closeInjuryModal = function() {
  const modalWrapper = document.getElementById('injury-modal-wrapper');
  if (modalWrapper) modalWrapper.classList.add('hidden');
};

window.toggleNoIssuesState = function() {
  window.markAsNoIssues();
};

window.markAsNoIssues = function() {
  const areaEl = document.getElementById('injury-problem-area');
  if (areaEl) areaEl.value = 'None';
  const sideEl = document.getElementById('injury-side-location');
  if (sideEl) sideEl.value = 'Bilateral';
  const painSlider = document.getElementById('injury-pain-slider');
  if (painSlider) {
    painSlider.value = 0;
    window.updatePainScoreDisplay(0);
  }
  const actionEl = document.getElementById('injury-action-status');
  if (actionEl) actionEl.value = 'Cleared';
  const notesEl = document.getElementById('injury-symptom-notes');
  if (notesEl) notesEl.value = 'No issues reported. 100% healthy.';
};

window.updatePainScoreDisplay = function(val) {
  const display = document.getElementById('pain-score-display');
  if (!display) return;
  const num = parseInt(val, 10);
  let label = 'Mild';
  if (num === 0) label = 'None';
  else if (num <= 3) label = 'Mild';
  else if (num <= 6) label = 'Moderate';
  else label = 'Severe';
  display.textContent = `${num} / 10 — ${label}`;
};

window.saveInjuryReport = function() {
  window.closeInjuryModal();
};

// ==========================================
// 3. CORE LOGIC: FETCH & RENDER RUNS
// ==========================================

async function fetchAndRenderRuns() {
  try {
    const res = await fetch('/api/runs');
    if (!res.ok) {
      throw new Error(`GET /api/runs failed: ${res.status}`);
    }
    const runs = await res.json();

    if (!runs || runs.length === 0) {
      await seedInitialWeekData();
      return;
    }

    cachedRuns = runs;
    renderRuns(cachedRuns);
    updateWeeklyTotals(cachedRuns);
  } catch (err) {
    console.error('Error fetching runs:', err);
  }
}

async function seedInitialWeekData() {
  const initialRuns = [
    {
      athleteName: "Carter",
      runDate: "2026-08-24",
      sessionSlot: "Run 1",
      mileage: 8.14,
      pace: "6:45",
      totalTime: "54:56",
      workoutType: "Normal Run",
      notes: "Aerobic threshold kick-off. Felt springy and controlled throughout the initial base loop."
    },
    {
      athleteName: "Carter",
      runDate: "2026-08-25",
      sessionSlot: "Run 1",
      mileage: 12.00,
      pace: "6:40",
      totalTime: "1:20:00",
      workoutType: "Long Run",
      notes: "Long Sunday continuous 12-miler. Rhythm felt clean, smooth turnover over rolling hills."
    },
    {
      athleteName: "Carter",
      runDate: "2026-08-26",
      sessionSlot: "Run 1",
      mileage: 9.33,
      pace: "6:50",
      totalTime: "1:03:52",
      workoutType: "Normal Run",
      notes: "Steady 9.33 miles. Legs recovered well from long run after good 8hr sleep."
    },
    {
      athleteName: "Carter",
      runDate: "2026-08-27",
      sessionSlot: "Run 1",
      mileage: 7.00,
      pace: "6:42",
      totalTime: "46:54",
      workoutType: "Normal Run",
      notes: "Midweek 7-mile maintenance. Shorter sleep last night; hydrated heavily throughout day."
    },
    {
      athleteName: "Carter",
      runDate: "2026-08-28",
      sessionSlot: "Run 1",
      mileage: 7.84,
      pace: "6:38",
      totalTime: "52:00",
      workoutType: "Normal Run",
      notes: "7.84 miles with hurdle mobility work afterwards. Hamstrings opened up well."
    },
    {
      athleteName: "Carter",
      runDate: "2026-08-29",
      sessionSlot: "Run 1",
      mileage: 11.51,
      pace: "6:44",
      totalTime: "1:17:30",
      workoutType: "Workout",
      notes: "Effort basically turns it into about 12.5 for sure. Strong hilly segment."
    }
  ];

  try {
    for (const run of initialRuns) {
      await fetch('/api/runs', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(run)
      });
    }
    const res = await fetch('/api/runs');
    cachedRuns = await res.json();
    renderRuns(cachedRuns);
    updateWeeklyTotals(cachedRuns);
  } catch (err) {
    console.error('Failed to seed initial week runs:', err);
  }
}

function renderRuns(runs) {
  WEEK_DATES.forEach(dateStr => {
    const shortDate = formatIsoToShort(dateStr);
    const [month, day] = shortDate.split('/');
    const slot1Id = `slot-${month}-${day}-1`;
    const slot2Id = `slot-${month}-${day}-2`;
    const commentId = `comment-${month}-${day}`;

    const slot1El = document.getElementById(slot1Id);
    const slot2El = document.getElementById(slot2Id);
    const commentEl = document.getElementById(commentId);

    const dayRuns = runs.filter(r => r.runDate === dateStr);
    const slot1Runs = dayRuns.filter(r => isSlot1(r.sessionSlot));
    const slot2Runs = dayRuns.filter(r => isSlot2(r.sessionSlot));

    dayRuns.forEach(r => {
      if (!isSlot1(r.sessionSlot) && !isSlot2(r.sessionSlot)) {
        if (slot1Runs.length === 0) slot1Runs.push(r);
        else slot2Runs.push(r);
      }
    });

    if (slot1El) {
      if (slot1Runs.length > 0) {
        slot1El.innerHTML = slot1Runs.map(run => createRunCardHtml(run)).join('');
      } else {
        slot1El.innerHTML = createEmptySlotHtml(dateStr, 'Run 1');
      }
    }

    if (slot2El) {
      if (slot2Runs.length > 0) {
        slot2El.innerHTML = slot2Runs.map(run => createRunCardHtml(run)).join('');
      } else {
        slot2El.innerHTML = createEmptySlotHtml(dateStr, 'Run 2');
      }
    }

    if (commentEl) {
      const activeNotes = dayRuns.map(r => r.notes).filter(Boolean).join(' | ');
      if (activeNotes) {
        commentEl.textContent = activeNotes;
      }
    }
  });
}

function isSlot1(slot) {
  if (!slot) return true;
  const s = slot.trim().toUpperCase();
  return s === 'RUN 1' || s === 'AM' || s === 'SESSION 1' || s === '1';
}

function isSlot2(slot) {
  if (!slot) return false;
  const s = slot.trim().toUpperCase();
  return s === 'RUN 2' || s === 'PM' || s === 'SESSION 2' || s === '2';
}

function createRunCardHtml(run) {
  const milesDisplay = Number(run.mileage || 0).toFixed(2);
  const paceDisplay = run.pace ? `${run.pace}/mi` : '—';
  const timeDisplay = run.totalTime ? `• ${run.totalTime}` : '';

  return `
    <div class="group flex items-center justify-between p-1.5 -m-1.5 rounded hover:bg-[#EEF3FD]/70 cursor-pointer transition-colors border border-transparent hover:border-[#D7E3FC] mb-1"
         onclick="openEditRunModal(${run.id})" title="Click to edit or delete run">
      <div>
        <div class="font-mono font-bold text-[12px] text-[#080808]">
          ${milesDisplay} <span class="text-[10px] font-normal text-[#70706B]">mi</span>
        </div>
        <div class="text-[10px] font-mono text-[#154ECD] font-medium leading-tight">
          ${paceDisplay} <span class="text-[#70706B] font-normal">${timeDisplay}</span>
        </div>
      </div>
      <span class="material-symbols-outlined text-[14px] text-[#A0A09A] group-hover:text-[#154ECD] transition-colors shrink-0">edit</span>
    </div>
  `;
}

function createEmptySlotHtml(dateStr, slotName) {
  return `
    <div class="group flex items-center justify-between p-1.5 -m-1.5 rounded hover:bg-[#FAFAF8] cursor-pointer transition-colors border border-transparent hover:border-[#DCDCD6]"
         onclick="openAddRunModal('${dateStr}', '${slotName}')" title="Click to log run for ${slotName}">
      <span class="text-[11px] font-mono text-[#8A8A85]">—</span>
      <span class="material-symbols-outlined text-[13px] text-transparent group-hover:text-[#A0A09A] transition-colors">add</span>
    </div>
  `;
}

// ==========================================
// 4. WEEKLY TOTALS & TELEMETRY
// ==========================================

function updateWeeklyTotals(runs) {
  let totalMiles = 0;
  let totalSeconds = 0;

  const dailyTotals = {
    '2026-08-24': 0,
    '2026-08-25': 0,
    '2026-08-26': 0,
    '2026-08-27': 0,
    '2026-08-28': 0,
    '2026-08-29': 0,
    '2026-08-30': 0
  };

  runs.forEach(r => {
    const mi = Number(r.mileage) || 0;
    totalMiles += mi;

    if (dailyTotals.hasOwnProperty(r.runDate)) {
      dailyTotals[r.runDate] += mi;
    }

    const runSecs = parseTimeToSeconds(r.totalTime) || (mi * (parseTimeToSeconds(r.pace) || 0));
    if (runSecs > 0) {
      totalSeconds += runSecs;
    }
  });

  const avgPaceSecs = totalMiles > 0 && totalSeconds > 0 ? (totalSeconds / totalMiles) : 0;
  const avgPaceStr = formatSecondsToPace(avgPaceSecs) || '6:42';

  const bannerEl = document.getElementById('weekly-total-banner');
  if (bannerEl) bannerEl.textContent = `${totalMiles.toFixed(2)} mi`;

  const goalDiffEl = document.getElementById('weekly-goal-diff');
  if (goalDiffEl) {
    const diff = totalMiles - 55.00;
    if (diff >= 0) {
      goalDiffEl.textContent = `+${diff.toFixed(2)} mi over goal`;
      goalDiffEl.className = "px-1.5 py-0.2 rounded text-[10px] font-mono font-semibold bg-[#EEF3FD] text-[#154ECD] border border-[#D7E3FC]";
    } else {
      goalDiffEl.textContent = `${Math.abs(diff).toFixed(2)} mi to goal`;
      goalDiffEl.className = "px-1.5 py-0.2 rounded text-[10px] font-mono font-semibold bg-[#FAFAF8] text-[#70706B] border border-[#DCDCD6]";
    }
  }

  const telemetryTotalEl = document.getElementById('weekly-total-telemetry');
  if (telemetryTotalEl) telemetryTotalEl.textContent = `${totalMiles.toFixed(2)} mi Total`;

  const telemetryPaceEl = document.getElementById('weekly-avg-pace-telemetry');
  if (telemetryPaceEl) telemetryPaceEl.textContent = `${avgPaceStr}/mi`;

  const footerValEl = document.getElementById('weekly-total-footer-val');
  if (footerValEl) footerValEl.textContent = `${totalMiles.toFixed(2)} mi`;

  const footerPaceEl = document.getElementById('weekly-avg-pace-footer');
  if (footerPaceEl) footerPaceEl.textContent = `avg ${avgPaceStr}/mi`;

  const maxDayMiles = Math.max(...Object.values(dailyTotals), 12.0);
  WEEK_DATES.forEach(dateStr => {
    const short = formatIsoToShort(dateStr);
    const [m, d] = short.split('/');
    const valEl = document.getElementById(`chart-val-${m}-${d}`);
    const barEl = document.getElementById(`chart-bar-${m}-${d}`);

    const dayMiles = dailyTotals[dateStr] || 0;

    if (valEl) {
      valEl.textContent = dayMiles.toFixed(2);
      if (dayMiles >= 11) {
        valEl.className = "text-[9px] text-[#154ECD] font-bold";
      } else if (dayMiles > 0) {
        valEl.className = "text-[9px] text-[#70706B]";
      } else {
        valEl.className = "text-[9px] text-[#8A8A85]";
      }
    }

    if (barEl) {
      if (dayMiles > 0) {
        const heightPx = Math.max(12, Math.round((dayMiles / maxDayMiles) * 96));
        barEl.style.height = `${heightPx}px`;
        barEl.className = "w-full max-w-[24px] rounded-t bg-[#154ECD]";
      } else {
        barEl.style.height = `8px`;
        barEl.className = "w-full max-w-[24px] rounded-t bg-[#F0F0EC] border border-dashed border-[#DCDCD6]";
      }
    }
  });
}

// ==========================================
// 5. HELPER UTILITIES
// ==========================================

function showModalDialog() {
  const modalWrapper = document.getElementById('run-edit-modal-wrapper');
  const modalDialog = document.getElementById('run-edit-dialog');

  if (modalWrapper && modalDialog) {
    modalWrapper.classList.remove('opacity-0', 'pointer-events-none');
    modalDialog.classList.remove('scale-95');
    modalDialog.classList.add('scale-100');
  }
}

function normalizeDateToIso(dateStr) {
  if (!dateStr) return '2026-08-24';
  const str = String(dateStr).trim();
  if (/^\d{4}-\d{2}-\d{2}$/.test(str)) {
    return str;
  }
  const parts = str.split('/');
  if (parts.length === 2) {
    const m = parts[0].padStart(2, '0');
    const d = parts[1].padStart(2, '0');
    return `2026-${m}-${d}`;
  } else if (parts.length === 3) {
    const m = parts[0].padStart(2, '0');
    const d = parts[1].padStart(2, '0');
    const y = parts[2].length === 2 ? `20${parts[2]}` : parts[2];
    return `${y}-${m}-${d}`;
  }
  return '2026-08-24';
}

function formatIsoToShort(isoStr) {
  if (!isoStr) return '8/24';
  const parts = isoStr.split('-');
  if (parts.length === 3) {
    const m = parseInt(parts[1], 10);
    const d = parseInt(parts[2], 10);
    return `${m}/${d}`;
  }
  return isoStr;
}

function parseTimeToSeconds(timeStr) {
  if (!timeStr) return null;
  const clean = String(timeStr).replace(/[^0-9:]/g, '').trim();
  if (!clean) return null;

  const parts = clean.split(':').map(Number);
  if (parts.some(isNaN)) return null;

  if (parts.length === 3) {
    return parts[0] * 3600 + parts[1] * 60 + parts[2];
  } else if (parts.length === 2) {
    return parts[0] * 60 + parts[1];
  } else if (parts.length === 1) {
    return parts[0] * 60;
  }
  return null;
}

function formatSecondsToTime(totalSecs) {
  if (!totalSecs || isNaN(totalSecs) || totalSecs <= 0) return '';
  const rounded = Math.round(totalSecs);
  const hrs = Math.floor(rounded / 3600);
  const rem = rounded % 3600;
  const mins = Math.floor(rem / 60);
  const secs = rem % 60;

  if (hrs > 0) {
    return `${hrs}:${String(mins).padStart(2, '0')}:${String(secs).padStart(2, '0')}`;
  }
  return `${mins}:${String(secs).padStart(2, '0')}`;
}

function formatSecondsToPace(paceSecs) {
  if (!paceSecs || isNaN(paceSecs) || paceSecs <= 0) return '';
  const rounded = Math.round(paceSecs);
  const mins = Math.floor(rounded / 60);
  const secs = rounded % 60;
  return `${mins}:${String(secs).padStart(2, '0')}`;
}

function initDrawerObserver() {
  const drawer = document.getElementById('quick-log-drawer');
  const backdrop = document.getElementById('drawer-backdrop');
  if (!drawer || !backdrop) return;

  const observer = new MutationObserver(mutations => {
    mutations.forEach(mutation => {
      if (mutation.attributeName === 'class') {
        if (!drawer.classList.contains('translate-x-full')) {
          backdrop.classList.remove('hidden');
        } else {
          backdrop.classList.add('hidden');
        }
      }
    });
  });

  observer.observe(drawer, { attributes: true });
}

function initKeyboardEscape() {
  document.addEventListener('keydown', e => {
    if (e.key === 'Escape') {
      window.closeAddRunModal();
      window.closeInjuryModal();
      const drawer = document.getElementById('quick-log-drawer');
      if (drawer) drawer.classList.add('translate-x-full');
    }
  });
}

function initApp() {
  initDrawerObserver();
  initKeyboardEscape();
  fetchAndRenderRuns();
}

// Auto-run when DOM is ready
if (document.readyState === 'loading') {
  document.addEventListener('DOMContentLoaded', initApp);
} else {
  initApp();
}
