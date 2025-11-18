// ------------------------
// 공통 포맷터
// ------------------------
function formatMoney(n) {
    if (n == null) return '';
    const num = Number(String(n).replace(/,/g, '').replace(/원/g, '').trim());
    if (Number.isNaN(num)) return '';
    return num.toLocaleString('ko-KR') + '원';
}

function formatYmdHis(s) {
    if (!s) return '';
    s = String(s);

    if (s.includes('T')) {
        const [date, time] = s.split('T');
        return `${date} ${time}`;
    }

    if (s.length >= 14) {
        return `${s.slice(0,4)}-${s.slice(4,6)}-${s.slice(6,8)} ` +
               `${s.slice(8,10)}:${s.slice(10,12)}:${s.slice(12,14)}`;
    }
    return '';
}

const favoriteSet = new Set();
let currentItem = null;

// ------------------------
// Toast 메시지
// ------------------------
function showToast(message, type = "success") {
    const toast = document.createElement("div");
    toast.className = `toast ${type}`;
    toast.textContent = message;
    document.body.appendChild(toast);
    setTimeout(() => toast.remove(), 2500);
}

// ------------------------
// 찜/오류 애니메이션 메시지
// ------------------------
function _ensureFavoriteAlert() {
    let container = document.querySelector(".favorite-alert");
    if (!container) {
        container = document.createElement("div");
        container.className = "favorite-alert";
        container.innerHTML = `
        <div class="inner">
            <div class="circle"><div class="ring"></div></div>
            <div class="check">✓</div>
            <div class="msg"></div>
        </div>`;
        document.body.appendChild(container);
    }
    return container;
}

function showFavoriteMessage(text, type = "check") {
    const el = _ensureFavoriteAlert();
    const inner = el.querySelector(".inner");
    const msg = el.querySelector(".msg");
    const circle = el.querySelector(".circle");
    const check = el.querySelector(".check");

    el.classList.remove("check", "shake", "show");
    inner.style.opacity = "1";
    msg.textContent = text;

    if (type === "check") {
        el.classList.remove("shake");
        circle.style.display = "block";
        check.style.opacity = "0";
        el.classList.add("show");
        setTimeout(() => {
            el.classList.add("check");
            circle.style.display = "none";
            check.style.opacity = "1";
        }, 600);
    } else if (type === "shake") {
        el.classList.remove("check");
        circle.style.display = "none";
        check.style.opacity = "0";
        el.classList.add("shake");
        el.classList.add("show");
    } else {
        el.classList.add("show");
    }

    clearTimeout(el._hideTimer);
    el._hideTimer = setTimeout(() => {
        el.classList.remove("show", "check", "shake");
        inner.style.opacity = "0";
    }, 1800);
}

// ------------------------
// 상세 조회 → 모달 표시 (webItem + webItemDetail 병합)
// ------------------------
function openDetail(cardEl) {
    currentItem = cardEl;
    const cltrNm = cardEl.dataset.cltrnm;
    if (!cltrNm) return;

    fetch(`/api/popupDetail?cltrNm=${encodeURIComponent(cltrNm)}`)
        .then(res => res.json())
        .then(items => {
            if (!items) {
                showFavoriteMessage("상세 정보를 찾을 수 없습니다.", "shake");
                return;
            }

            if (!Array.isArray(items)) items = [items];
            if (items.length === 0) {
                showFavoriteMessage("상세 정보를 찾을 수 없습니다.", "shake");
                return;
            }

            const main = items[0];

            let history = [];
            if (main.history && Array.isArray(main.history) && main.history.length > 0) {
                history = main.history.slice(0, 3);
            } else {
                history = items
                    .filter(d => d.cltrHstrNo !== undefined && d.cltrHstrNo !== null)
                    .slice(0, 3);
            }

            const merged = { ...main, ...(history[0] || {}) };

            const modal = document.getElementById('detailModal');
            if (!modal) return;

            // 공통 필드
            document.getElementById('modal-cltrNm').textContent = merged.cltrNm || '';
            document.getElementById('modal-address').textContent = merged.ldnAdrs || merged.nmrdAdrs || '';
            document.getElementById('modal-appraisal').textContent = formatMoney(merged.apslAsesAmt || merged.apslAsesAvgAmt);
            document.getElementById('modal-minBid').textContent = formatMoney(merged.minBidPrc);
            document.getElementById('modal-status').textContent = merged.pbctStatNm || merged.pbctCltrStatNm || '';
            document.getElementById('modal-begin').textContent = formatYmdHis(merged.pbctBegnDtm);
            document.getElementById('modal-end').textContent = formatYmdHis(merged.pbctClsDtm);

            // webItemDetail 동적 필드
            const labelMap = {
                rnum: "RNUM",
                pbctCdtnNo: "조건번호",
                cltrNo: "물건번호",
                cltrHstrNo: "이력번호",
                scrnGrpCd: "화면 그룹 코드",
                ctgrFullNm: "카테고리",
                bidMnmtNo: "입찰 문서 번호",
                cltrMnmtNo: "CLTR 문서번호",
                nmrdAdrs: "지번주소",
                ldnmPnu: "토지번호",
                dpslMtdCd: "처분 방법 코드",
                dpslMtdNm: "처분 방법",
                bidMtdNm: "입찰 방식",
                apslAsesAvgAmt: "평가금액",
                feeRate: "수수료율",
                pbctCltrStatNm: "입찰 상태",
                uscbCnt: "조회자 수",
                iqryCnt: "조회 수",
                goodsNm: "물건명",
                cltrImgFiles: "이미지 파일",
                plnmNo: "물건번호",
                pbctNo: "공고번호"
            };

            const modalExtra = document.getElementById('modal-extra-info');
            modalExtra.innerHTML = '';
            Object.keys(merged).forEach(key => {
                if (!merged[key] || ['cltrNm','ldnAdrs','apslAsesAmt','minBidPrc','pbctStatNm','pbctBegnDtm','pbctClsDtm','imgUrl','history'].includes(key)) return;
                const p = document.createElement('p');
                const label = labelMap[key] || key;
                p.innerHTML = `<strong>${label}:</strong> ${merged[key]}`;
                modalExtra.appendChild(p);
            });

            // 입찰 이력 테이블
            const bidHistoryDiv = document.getElementById('modal-bid-history');
            bidHistoryDiv.innerHTML = '';
            if (history && history.length > 0) {
                const table = document.createElement('table');
                table.className = 'bid-history-table';
                const thead = document.createElement('thead');
                thead.innerHTML = '<tr><th>입찰 시작</th><th>입찰 마감</th><th>최저입찰가</th></tr>';
                table.appendChild(thead);
                const tbody = document.createElement('tbody');
                history.forEach(d => {
                    const tr = document.createElement('tr');
                    tr.innerHTML = `<td>${formatYmdHis(d.pbctBegnDtm)}</td>
                                    <td>${formatYmdHis(d.pbctClsDtm)}</td>
                                    <td>${formatMoney(d.minBidPrc)}</td>`;
                    tbody.appendChild(tr);
                });
                table.appendChild(tbody);
                bidHistoryDiv.appendChild(table);
            }

            // 이미지 처리
            const imgEl = document.getElementById('modal-img');
            const fallback = document.getElementById('modal-img-fallback');
            if (merged.imgUrl && merged.imgUrl.trim() !== '') {
                imgEl.src = merged.imgUrl;
                imgEl.style.display = 'block';
                fallback.style.display = 'none';
            } else {
                imgEl.removeAttribute('src');
                imgEl.style.display = 'none';
                fallback.style.display = 'flex';
            }

            // 전역 변수
            window.plnmNo = merged.plnmNo;
            window.pbctNo = merged.pbctNo;
            window.itemName = merged.cltrNm || '';
            window.minBidPrice = merged.minBidPrc;

            modal.classList.add('open');
        })
        .catch(err => {
            console.error(err);
            showFavoriteMessage("상세 정보를 불러오는데 실패했습니다.", "shake");
        });
}

function closeModal() {
    const modal = document.getElementById('detailModal');
    if (modal) modal.classList.remove('open');
}

// ------------------------
// 찜 기능
// ------------------------
function addFavorite(btn) {
    let plnmNo = btn.getAttribute("data-plnmno");
    let pbctNo = btn.getAttribute("data-pbctno");

    if ((!plnmNo || !pbctNo) && currentItem) {
        plnmNo = currentItem.dataset.plnmno;
        pbctNo = currentItem.dataset.pbctno;
    }

    if (!plnmNo || !pbctNo) {
        showFavoriteMessage("로그인 후 이용해주세요.", "shake");
        return;
    }

    const uniqueKey = `${plnmNo}_${pbctNo}`;
    if (favoriteSet.has(uniqueKey)) {
        showFavoriteMessage("이미 찜한 물건입니다.", "shake");
        return;
    }

    fetch(`/favorite/add?plnmNo=${encodeURIComponent(plnmNo)}&pbctNo=${encodeURIComponent(pbctNo)}`, { method: "POST" })
        .then(async res => {
            const text = await res.text();
            if (!res.ok && text) throw new Error(text);
            return text;
        })
        .then(msg => {
            if (!msg) return;
            if (msg.includes("로그인")) showFavoriteMessage("로그인 후 이용해주세요.", "shake");
            else if (msg.includes("이미") || msg.includes("중복")) showFavoriteMessage("이미 찜한 물건입니다.", "shake");
            else {
                favoriteSet.add(uniqueKey);
                showFavoriteMessage("찜 목록에 추가되었습니다!", "check");
            }
        })
        .catch(() => showFavoriteMessage("로그인 후 이용해주세요.", "shake"));
}

// ------------------------
// 입찰 기능
// ------------------------
function bidItem(btn) {
    let plnmNo = btn.getAttribute("data-plnmno");
    let pbctNo = btn.getAttribute("data-pbctno");

    if ((!plnmNo || !pbctNo) && currentItem) {
        plnmNo = currentItem.dataset.plnmno;
        pbctNo = currentItem.dataset.pbctno;
    }

    if (!plnmNo || !pbctNo) {
        showFavoriteMessage("로그인 후 이용해주세요.", "shake");
        return;
    }

    fetch(`/bid/apply?plnmNo=${encodeURIComponent(plnmNo)}&pbctNo=${encodeURIComponent(pbctNo)}`, { method: "POST" })
        .then(async res => {
            const text = await res.text();
            if (!res.ok && text) throw new Error(text);
            return text;
        })
        .then(msg => {
            if (!msg) return;
            if (msg.includes("로그인")) showFavoriteMessage("로그인 후 이용해주세요.", "shake");
            else if (msg.includes("이미") || msg.includes("중복")) showFavoriteMessage("이미 입찰한 물건입니다.", "shake");
            else showFavoriteMessage("입찰이 완료되었습니다!", "check");
        })
        .catch(() => showFavoriteMessage("로그인 후 이용해주세요.", "shake"));
}

function goBidPage() {
    if (!window.isLoggedIn) {
        showFavoriteMessage("로그인 후 이용해주세요", "shake");
        return;
    }

    let plnmNo = window.plnmNo || (currentItem ? currentItem.dataset.plnmno : null);
    let pbctNo = window.pbctNo || (currentItem ? currentItem.dataset.pbctno : null);
    let itemName = window.itemName || (currentItem ? currentItem.dataset.cltrnm : '');
    let minBidPrice = window.minBidPrice || (currentItem ? currentItem.querySelector('.price.min')?.textContent : null);

    if (!plnmNo || !pbctNo) {
        alert("잘못된 접근입니다.");
        return;
    }

    // ----------------------------
    // 여기서 콤마와 "원" 제거
    if (minBidPrice) {
        minBidPrice = minBidPrice.replace(/,/g, '').replace(/원/g, '').trim();
    }
    // ----------------------------

    const url = `/api/bid?plnmNo=${encodeURIComponent(plnmNo)}&pbctNo=${encodeURIComponent(pbctNo)}` +
                `&itemName=${encodeURIComponent(itemName)}&minBidPrice=${encodeURIComponent(minBidPrice)}`;
    location.href = url;
}


// ------------------------
// window export
// ------------------------
window.openDetail = openDetail;
window.closeModal = closeModal;
window.addFavorite = addFavorite;
window.bidItem = bidItem;
window.goBidPage = goBidPage;
