(() => {
  "use strict";
  const svg = document.querySelector(".room-canvas");
  if (!svg) return;

  let active = null;
  let moved = false;
  let startPointer = null;
  let startItem = null;
  const roomWidth = 480;
  const roomHeight = 320;

  function svgPoint(event) {
    const point = svg.createSVGPoint();
    point.x = event.clientX;
    point.y = event.clientY;
    return point.matrixTransform(svg.getScreenCTM().inverse());
  }

  function selectItem(item) {
    document.querySelectorAll(".room-item").forEach(el => el.classList.remove("drag-selected"));
    item.classList.add("drag-selected");
  }

  svg.querySelectorAll(".room-item").forEach(item => {
    item.style.cursor = "grab";
    item.style.touchAction = "none";
    item.addEventListener("pointerdown", event => {
      if (event.button !== 0) return;
      event.preventDefault();
      active = item;
      moved = false;
      startPointer = svgPoint(event);
      startItem = { x: Number(item.dataset.x), y: Number(item.dataset.y) };
      item.setPointerCapture?.(event.pointerId);
      item.style.cursor = "grabbing";
      selectItem(item);
    });

    item.addEventListener("pointermove", event => {
      if (active !== item || !startPointer || !startItem) return;
      const point = svgPoint(event);
      const dx = point.x - startPointer.x;
      const dy = point.y - startPointer.y;
      if (Math.abs(dx) + Math.abs(dy) > 2) moved = true;
      if (!moved) return;
      const width = Number(item.querySelector("rect")?.getAttribute("width") || 60);
      const height = Number(item.querySelector("rect")?.getAttribute("height") || 40);
      const x = Math.max(0, Math.min(roomWidth - width, Math.round(startItem.x + dx)));
      const y = Math.max(0, Math.min(roomHeight - height, Math.round(startItem.y + dy)));
      item.dataset.pendingX = String(x);
      item.dataset.pendingY = String(y);
      item.setAttribute("transform", `translate(${x + width / 2 + 20} ${y + height / 2 + 20}) rotate(${getRotation(item)})`);
    });

    item.addEventListener("pointerup", async event => {
      if (active !== item) return;
      item.style.cursor = "grab";
      const shouldSave = moved;
      active = null;
      startPointer = null;
      startItem = null;
      if (!shouldSave) {
        const form = document.createElement("form");
        form.method = "post";
        form.action = "/furniture/select";
        const input = document.createElement("input");
        input.type = "hidden"; input.name = "id"; input.value = item.dataset.id;
        form.appendChild(input); document.body.appendChild(form); form.submit();
        return;
      }
      const body = new URLSearchParams({
        id: item.dataset.id,
        x: item.dataset.pendingX || item.dataset.x,
        y: item.dataset.pendingY || item.dataset.y
      });
      try {
        const response = await fetch("/furniture/drag", {
          method: "POST",
          headers: { "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8" },
          body
        });
        if (!response.ok) throw new Error(`Server returned ${response.status}`);
        window.location.reload();
      } catch (error) {
        console.error("Could not save furniture position:", error);
        window.alert("Could not save that position. Please check the server and try again.");
        window.location.reload();
      }
    });

    item.addEventListener("pointercancel", () => {
      if (active === item) window.location.reload();
    });
    item.addEventListener("keydown", event => {
      if (event.key === "Enter" || event.key === " ") {
        event.preventDefault();
        const form = document.createElement("form");
        form.method = "post"; form.action = "/furniture/select";
        const input = document.createElement("input");
        input.type = "hidden"; input.name = "id"; input.value = item.dataset.id;
        form.appendChild(input); document.body.appendChild(form); form.submit();
      }
    });
  });

  function getRotation(item) {
    const match = (item.getAttribute("transform") || "").match(/rotate\((-?\d+(?:\.\d+)?)\)/);
    return match ? match[1] : "0";
  }
})();
