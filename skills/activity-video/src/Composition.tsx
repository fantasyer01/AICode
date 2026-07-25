import React from "react";
import {
  AbsoluteFill,
  useVideoConfig,
  useCurrentFrame,
  interpolate,
  spring,
} from "remotion";
import { TransitionSeries, linearTiming } from "@remotion/transitions";
import { fade } from "@remotion/transitions/fade";

const HABITS = [
  "习惯 1: 积极主动 (Be Proactive)",
  "习惯 2: 以终为始 (Begin with the End in Mind)",
  "习惯 3: 要事第一 (Put First Things First)",
  "习惯 4: 双赢思维 (Think Win-Win)",
  "习惯 5: 知彼解己 (Seek First to Understand...)",
  "习惯 6: 统合综效 (Synergize)",
  "习惯 7: 不断更新 (Sharpen the Saw)",
];

export const MyVideo = () => {
  const { fps } = useVideoConfig();

  return (
    <AbsoluteFill
      style={{
        backgroundColor: "#0a0a0a",
        color: "white",
        fontFamily: "system-ui, -apple-system, sans-serif",
      }}
    >
      <TransitionSeries>
        <TransitionSeries.Sequence durationInFrames={60}>
          <TitleScene />
        </TransitionSeries.Sequence>
        <TransitionSeries.Transition
          presentation={fade()}
          timing={linearTiming({ durationInFrames: 15 })}
        />

        {HABITS.map((habit, index) => (
          <React.Fragment key={index}>
            <TransitionSeries.Sequence durationInFrames={40}>
              <HabitScene text={habit} index={index + 1} />
            </TransitionSeries.Sequence>
            {index < HABITS.length - 1 && (
              <TransitionSeries.Transition
                presentation={fade()}
                timing={linearTiming({ durationInFrames: 5 })}
              />
            )}
          </React.Fragment>
        ))}

        <TransitionSeries.Transition
          presentation={fade()}
          timing={linearTiming({ durationInFrames: 15 })}
        />
        <TransitionSeries.Sequence durationInFrames={50}>
          <OutroScene />
        </TransitionSeries.Sequence>
      </TransitionSeries>
    </AbsoluteFill>
  );
};

const TitleScene = () => {
  const frame = useCurrentFrame();
  const { fps } = useVideoConfig();
  const opacity = interpolate(frame, [0, 30], [0, 1], {
    extrapolateRight: "clamp",
  });
  const scale = spring({
    frame,
    fps,
    config: {
      damping: 20,
    },
  });

  return (
    <AbsoluteFill
      style={{
        justifyContent: "center",
        alignItems: "center",
        background: "linear-gradient(to bottom, #1a1a1a, #000)",
      }}
    >
      <div
        style={{
          opacity,
          transform: `scale(${scale})`,
          textAlign: "center",
        }}
      >
        <h1 style={{ fontSize: 100, marginBottom: 20 }}>
          《高效能人士的七个习惯》
        </h1>
        <p style={{ fontSize: 40, color: "#aaa" }}>
          史蒂芬·柯维 (Stephen Covey)
        </p>
      </div>
    </AbsoluteFill>
  );
};

const HabitScene = ({ text, index }: { text: string; index: number }) => {
  const frame = useCurrentFrame();
  const { fps } = useVideoConfig();

  const springConfig = {
    damping: 12,
  };

  const scale = spring({
    frame,
    fps,
    config: springConfig,
    durationInFrames: 30,
  });

  const opacity = interpolate(frame, [0, 15], [0, 1], {
    extrapolateRight: "clamp",
  });

  return (
    <AbsoluteFill
      style={{
        justifyContent: "center",
        alignItems: "center",
        background: "radial-gradient(circle, #222 0%, #000 100%)",
      }}
    >
      <div
        style={{
          opacity,
          transform: `scale(${scale})`,
          fontSize: 70,
          fontWeight: "bold",
          textAlign: "center",
          padding: "0 100px",
          lineHeight: 1.4,
        }}
      >
        <div style={{ color: "#3498db", fontSize: 40, marginBottom: 20 }}>
          Habit {index}
        </div>
        {text}
      </div>
    </AbsoluteFill>
  );
};

const OutroScene = () => {
  const frame = useCurrentFrame();
  const { fps } = useVideoConfig();

  const opacity = interpolate(frame, [0, 30], [0, 1], {
    extrapolateRight: "clamp",
  });

  const scale = spring({
    frame,
    fps,
    config: {
      damping: 20,
    },
    delay: 15,
  });

  return (
    <AbsoluteFill
      style={{
        justifyContent: "center",
        alignItems: "center",
        background: "linear-gradient(to top, #1a1a1a, #000)",
      }}
    >
      <div style={{ opacity, textAlign: "center" }}>
        <h2
          style={{
            fontSize: 80,
            transform: `scale(${scale})`,
            background: "linear-gradient(45deg, #3498db, #2ecc71)",
            WebkitBackgroundClip: "text",
            WebkitTextFillColor: "transparent",
          }}
        >
          开始改变，成就高效人生
        </h2>
      </div>
    </AbsoluteFill>
  );
};
